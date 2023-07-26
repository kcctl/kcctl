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
package org.kcctl.command;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.kcctl.IntegrationTest;
import org.kcctl.IntegrationTestProfile;
import org.kcctl.service.ConnectorOffset;
import org.kcctl.service.ConnectorOffsets;
import org.kcctl.support.InjectCommandContext;
import org.kcctl.support.KcctlCommandContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import io.debezium.testing.testcontainers.DebeziumContainer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GetOffsetsCommandTest extends IntegrationTest {

    private static final long OFFSET_AVAILABILITY_TIMEOUT_SECONDS = 30;

    private final ObjectMapper mapper = new ObjectMapper();

    @InjectCommandContext
    KcctlCommandContext<GetOffsetsCommand> context;

    @BeforeAll
    public static void prepare() {
        // TODO: replace explicit container tag with DebeziumContainer.latestStable() once Debezium releases 2.4, which is based on Kafka 3.5
        kafkaConnect = new DebeziumContainer("debezium/connect:2.4.0.Alpha1")
                .withNetwork(network)
                .withKafka(kafka)
                .dependsOn(kafka)
                // Force rapid offset commits, in order for offsets to appear quickly in the REST API
                .withEnv("OFFSET_FLUSH_INTERVAL_MS", "100");
        IntegrationTest.prepare();
    }

    @Test
    public void should_list_offsets_single_connector() throws Exception {
        registerTestConnector("test1");

        await()
                .atMost(Duration.ofSeconds(OFFSET_AVAILABILITY_TIMEOUT_SECONDS))
                .until(() -> {
                    context.reset();
                    int exitCode = context.commandLine().execute("test1");
                    assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
                    String output = context.output().toString();
                    ConnectorOffsets offsets = mapper.readValue(output, ConnectorOffsets.class);

                    if (offsets.offsets().isEmpty()) {
                        // No offsets have been committed yet; wait a little longer for an offset commit to complete
                        return false;
                    }

                    Map<String, Object> expectedPartition = Map.of(
                            "sourceClusterAlias", "source",
                            "targetClusterAlias", "target");
                    Map<String, Object> expectedOffset = Map.of(
                            "offset", 0);
                    ConnectorOffsets expectedOffsets = new ConnectorOffsets(List.of(
                            new ConnectorOffset(expectedPartition, expectedOffset)));
                    assertThat(offsets).isEqualTo(expectedOffsets);

                    // Success!
                    return true;
                });
    }

    @Test
    public void should_list_offsets_multiple_connectors() throws Exception {
        // IMPORTANT: None of the connector names we use in these tests should overlap,
        // even across different test cases. This is because offsets are persisted even after
        // connectors are deleted
        registerTestConnector("test2");
        registerTestConnector("test3");
        registerTestConnector("test4");

        await()
                .atMost(Duration.ofSeconds(OFFSET_AVAILABILITY_TIMEOUT_SECONDS))
                .until(() -> {
                    context.reset();
                    int exitCode = context.commandLine().execute("test2", "test3");
                    assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
                    String output = context.output().toString();

                    // Small hack: want to make sure that there's at least one partition/offset
                    // pair for each connector
                    // We know that the MirrorHeartbeatConnector that we've configured should
                    // only ever emit one kind of source partition, so just checking to see if
                    // "partition" occurs twice in our output does the trick for now
                    return output.matches("(?s).*\"partition\".*\"partition\".*");
                });
    }

}
