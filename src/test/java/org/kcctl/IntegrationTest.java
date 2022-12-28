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
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;
import org.kcctl.support.InjectCommandContext;
import org.kcctl.support.KcctlCommandContext;
import org.kcctl.util.ConfigurationContext;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import io.debezium.testing.testcontainers.ConnectorConfiguration;
import io.debezium.testing.testcontainers.DebeziumContainer;
import picocli.CommandLine;

public abstract class IntegrationTest {

    public static final String HEARTBEAT_TOPIC = "heartbeat-test";

    protected static final Network network = Network.newNetwork();

    protected static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.1"))
            .withNetwork(network);

    protected static DebeziumContainer kafkaConnect = DebeziumContainer.latestStable()
            .withNetwork(network)
            .withKafka(kafka)
            .dependsOn(kafka);

    @BeforeAll
    public static void prepare() {
        Startables.deepStart(Stream.of(kafka, kafkaConnect)).join();
    }

    public String getConnectVersion() throws Exception {
        Container.ExecResult result = kafkaConnect.execInContainer("/bin/bash", "-c", "/usr/bin/printenv KAFKA_VERSION");
        return result.getStdout().replace("\n", "");
    }

    @BeforeEach
    void injectCommandContext() {
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
