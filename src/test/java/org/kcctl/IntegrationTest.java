/*
 *  Copyright 2021 The original authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kcctl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;
import org.kcctl.support.InjectCommandContext;
import org.kcctl.support.KcctlCommandContext;
import org.kcctl.support.SkipIfConnectVersionIsOlderThan;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Version;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import io.debezium.testing.testcontainers.ConnectorConfiguration;
import io.debezium.testing.testcontainers.DebeziumContainer;
import io.debezium.testing.testcontainers.util.ContainerImageVersions;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

public abstract class IntegrationTest {

    public static final String DEBEZIUM_IMAGE = "debezium/connect";
    public static final String CONNECT_VERSION_VAR = "CONNECT_VERSION";
    public static final String NIGHTLY_BUILD = "NIGHTLY";
    public static final String TRUNK_BUILD = "TRUNK";
    public static final String LATEST_STABLE_BUILD = "LATEST_STABLE";
    public static final String KAFKA_VERSION_VAR = "KAFKA_VERSION";
    public static final String DEBEZIUM_VERSION_VAR = "DEBEZIUM_VERSION";
    public static final String HEARTBEAT_TOPIC = "heartbeat-test";

    protected static final Network network = Network.newNetwork();

    protected static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.1"))
            .withNetwork(network);

    protected static final DebeziumContainer kafkaConnect = debeziumContainer();

    private static String kafkaConnectVersion;
    private static String debeziumVersion;

    @BeforeAll
    public static void prepareTestSuite() throws Exception {
        Startables.deepStart(Stream.of(kafka, kafkaConnect)).join();

        kafkaConnectVersion = readVariable(kafkaConnect, KAFKA_VERSION_VAR);
        debeziumVersion = readVariable(kafkaConnect, DEBEZIUM_VERSION_VAR);
    }

    public static String getConnectVersion() {
        return kafkaConnectVersion;
    }

    public static String getDebeziumVersion() {
        return debeziumVersion;
    }

    private static DebeziumContainer debeziumContainer() {
        String connectVersion = Optional.ofNullable(System.getenv(CONNECT_VERSION_VAR))
                .orElse(LATEST_STABLE_BUILD);

        String debeziumTag = switch (connectVersion) {
            case TRUNK_BUILD -> TRUNK_BUILD;
            case NIGHTLY_BUILD -> "nightly";
            case LATEST_STABLE_BUILD -> ContainerImageVersions.getStableVersion(IntegrationTest.DEBEZIUM_IMAGE);
            case "3.6" -> "2.5.0.Final";
            case "3.5" -> "2.4.2.Final";
            case "3.4" -> "2.3.0.Final";
            case "3.3" -> "2.1.4.Final";
            case "3.2" -> "1.9.7.Final";
            case "3.1" -> "1.9.3.Final";
            case "3.0" -> "1.8.1.Final";
            case "2.8" -> "1.7.2.Final";
            default -> throw new IllegalArgumentException("Kafka Connect version " + connectVersion + " is not yet supported");
        };

        DebeziumContainer debeziumContainer = TRUNK_BUILD.equals(debeziumTag) ? new ConnectTrunkDebeziumContainer() : new DebeziumContainer(DEBEZIUM_IMAGE + ":" + debeziumTag);

        return debeziumContainer
                .withNetwork(network)
                .withKafka(kafka)
                .dependsOn(kafka)
                // Force rapid offset commits, in order for offsets to appear quickly in the REST API for the 'get offsets' command tests
                .withEnv("OFFSET_FLUSH_INTERVAL_MS", "1000");
    }

    private static String readVariable(GenericContainer<?> container, String envVar) throws IOException, InterruptedException {

        String connectVersion = Optional.ofNullable(System.getenv(CONNECT_VERSION_VAR))
                .orElse(LATEST_STABLE_BUILD);
        String command = KAFKA_VERSION_VAR.equals(envVar) && TRUNK_BUILD.equals(connectVersion) ? "cat /kafka/kafka_version" : "/usr/bin/printenv " + envVar;

        return container
                .execInContainer("/bin/bash", "-c", command)
                .getStdout()
                .replace("\n", "");

    }

    @BeforeEach
    void prepareTest(TestInfo testInfo) {
        injectCommandContext();
        checkKafkaConnectVersion(testInfo);
    }

    private void checkKafkaConnectVersion(TestInfo testInfo) {
        SkipIfConnectVersionIsOlderThan versionConstraint = testInfo.getTestMethod()
                .map(method -> method.getAnnotation(SkipIfConnectVersionIsOlderThan.class))
                .orElse(getClass().getAnnotation(SkipIfConnectVersionIsOlderThan.class));

        // Neither method nor class is annotated; run the test regardless of which version of Kafka Connect we're targeting
        if (versionConstraint == null)
            return;

        Version requiredVersion = new Version(versionConstraint.value());
        Version actualVersion = new Version(kafkaConnectVersion);

        assumeTrue(
                actualVersion.greaterOrEquals(requiredVersion),
                "Current version of Kafka Connect is too old to run this test");
    }

    private void injectCommandContext() {
        ReflectionUtils.findFields(getClass(), it -> it.isAnnotationPresent(InjectCommandContext.class), HierarchyTraversalMode.TOP_DOWN)
                .forEach(field -> {
                    try {
                        KcctlCommandContext<?> commandContext = prepareContext(field);
                        ReflectionUtils.makeAccessible(field);
                        field.set(this, commandContext);
                    }
                    catch (IllegalAccessException e) {
                        throw new IntegrationTestException("Couldn't inject KcctlCommandContext", e);
                    }
                });
    }

    @AfterEach
    public void cleanup() {
        kafkaConnect.deleteAllConnectors();
    }

    protected void registerTestConnectors(String name, String... names) {
        registerTestConnector(name);
        for (String extraName : names) {
            registerTestConnector(extraName);
        }
    }

    protected void registerTestConnector(String name) {
        ConnectorConfiguration config = ConnectorConfiguration.create()
                .with("connector.class", "org.apache.kafka.connect.mirror.MirrorHeartbeatConnector")
                .with("tasks.max", 1)
                .with("source.cluster.alias", "source")
                .with("topic", HEARTBEAT_TOPIC)
                .with("admin.bootstrap.servers", getKafkaBootstrapServers());

        kafkaConnect.registerConnector(name, config);
    }

    protected String getKafkaBootstrapServers() {
        return "%s:9092".formatted(kafka.getNetworkAliases().get(0));
    }

    private KcctlCommandContext<?> prepareContext(Field field) {
        Type type = field.getGenericType();
        Type genericType = ((ParameterizedType) type).getActualTypeArguments()[0];
        Class<?> targetCommand = (Class<?>) genericType;

        ensureCaseyCommand(targetCommand);

        var context = initializeConfigurationContext();
        var command = instantiateCommand(targetCommand, context);
        var commandLine = new CommandLine(command);
        var output = new StringWriter();
        commandLine.setOut(new PrintWriter(output));
        var error = new StringWriter();
        commandLine.setErr(new PrintWriter(error));

        return new KcctlCommandContext<>(command, commandLine, output, error);
    }

    private void ensureCaseyCommand(Class<?> targetCommand) {
        if (!targetCommand.isAnnotationPresent(CommandLine.Command.class)) {
            throw new IntegrationTestException("KcctlCommandContext should target a type annotated with @CommandLine.Command");
        }
    }

    private ConfigurationContext initializeConfigurationContext() {
        try {
            var tempDir = Files.createTempDirectory("kcctl-test");
            var configFile = tempDir.resolve(".kcctl");

            Files.writeString(configFile, String.format("""
                    {
                        "currentContext": "local",
                        "local": {
                            "cluster": "%s",
                            "username": "testuser",
                            "password": "testpassword"
                        }
                    }
                    """, kafkaConnect.getTarget()));

            return new ConfigurationContext(tempDir.toFile());
        }
        catch (IOException e) {
            throw new IntegrationTestException("Couldn't initialize configuration context", e);
        }
    }

    private Object instantiateCommand(Class<?> targetCommand, ConfigurationContext configurationContext) {
        try {
            Constructor<?> constructor = targetCommand.getDeclaredConstructor(ConfigurationContext.class);
            return constructor.newInstance(configurationContext);
        }
        catch (NoSuchMethodException e) {
            throw new IntegrationTestException("Unsupported @CommandLine.Command type. Required a single argument constructor accepting a ConfigurationContext");
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IntegrationTestException("Couldn't instantiate command of type " + targetCommand, e);
        }
    }

    public static class IntegrationTestException extends RuntimeException {

        public IntegrationTestException(String msg) {
            super(msg);
        }

        public IntegrationTestException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
