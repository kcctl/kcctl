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

import java.io.IOException;
import java.time.Duration;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.kcctl.IntegrationTest;
import org.kcctl.IntegrationTestProfile;
import org.kcctl.service.ConnectorOffsets;
import org.kcctl.support.InjectCommandContext;
import org.kcctl.support.KcctlCommandContext;
import org.kcctl.support.SkipIfConnectVersionIsOlderThan;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import io.debezium.testing.testcontainers.Connector;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SkipIfConnectVersionIsOlderThan("3.6")
class PatchOffsetsCommandTest extends IntegrationTest {

    private static final long OFFSET_AVAILABILITY_TIMEOUT_SECONDS = 30;

    private final ObjectMapper mapper = new ObjectMapper();

    @InjectCommandContext
    KcctlCommandContext<PatchOffsetsCommand> patchContext;

    @InjectCommandContext
    KcctlCommandContext<GetOffsetsCommand> getContext;

    @InjectCommandContext
    KcctlCommandContext<StopConnectorCommand> stopContext;

    @Test
    public void patch_offsets_of_running_connector_should_return_error() {

        registerTestConnector("patch-offsets-test1");

        await()
                .atMost(Duration.ofSeconds(OFFSET_AVAILABILITY_TIMEOUT_SECONDS))
                .until(() -> {
                    patchContext.reset();

                    // No offsets have been committed yet; wait a little longer for an offset commit to complete
                    return isOffsetAvailable("patch-offsets-test1");
                });

        int exitCode = patchContext.commandLine().execute("--source-partition", "{}", "--source-offset", "{}", "patch-offsets-test1");
        String errorMessage = patchContext.error().toString();

        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.SOFTWARE);
        assertThat(errorMessage).contains(
                "Connectors must be in the STOPPED state before their offsets can be modified. This can be done for the specified connector by issuing a 'PUT' request to the '/connectors/patch-offsets-test1/stop' endpoint");
    }

    @Test
    public void offsets_are_correctly_patched() throws IOException {

        registerTestConnector("patch-offsets-test1");

        await()
                .atMost(Duration.ofSeconds(OFFSET_AVAILABILITY_TIMEOUT_SECONDS))
                .until(() -> {
                    patchContext.reset();

                    // No offsets have been committed yet; wait a little longer for an offset commit to complete
                    return isOffsetAvailable("patch-offsets-test1");
                });

        // Sanity check: make sure that the connector has only published one kind of source offset
        assertEquals(1, getOffsets("patch-offsets-test1").offsets().size());

        stopContext.runAndEnsureExitCodeOk("patch-offsets-test1");
        kafkaConnect.ensureConnectorState("patch-offsets-test1", Connector.State.STOPPED);

        String sourcePartition = "{\"sourceClusterAlias\": \"kcctl-test-src\", \"targetClusterAlias\": \"kcctl-test-dst\"}";
        String sourceOffset = "{\"offset\": 0}";
        int exitCode = patchContext.commandLine().execute(
                "--source-partition", sourcePartition,
                "--source-offset", sourceOffset,
                "patch-offsets-test1");
        String output = patchContext.output().toString();

        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(output).isEqualTo("The offsets for this connector have been altered successfully\n");

        // Sanity check: make sure that the second offset can be read back
        await()
                .atMost(Duration.ofSeconds(OFFSET_AVAILABILITY_TIMEOUT_SECONDS))
                .until(() -> getOffsets("patch-offsets-test1").offsets().size() == 2);
    }

    @Test
    public void require_offset_argument() {
        int exitCode = patchContext.commandLine().execute("some-connector");
        assertEquals(CommandLine.ExitCode.USAGE, exitCode);
        assertThat(patchContext.error().toString()).contains("Missing required argument ");
    }

    @Test
    public void disallow_multiple_offset_arguments() {
        String sourcePartition = "{\"sourceClusterAlias\": \"kcctl-test-src\", \"targetClusterAlias\": \"kcctl-test-dst\"}";
        String sourceOffset = "{\"offset\": 0}";
        int exitCode = patchContext.commandLine().execute(
                "--source-partition", sourcePartition,
                "--source-offset", sourceOffset,
                "--kafka-topic", "topic",
                "--kafka-partition", "0",
                "--kafka-offset", "0",
                "some-connector");
        assertEquals(CommandLine.ExitCode.USAGE, exitCode);
        assertThat(patchContext.error().toString()).contains("are mutually exclusive ");
    }

    private boolean isOffsetAvailable(String connectorName) throws IOException {
        return !getOffsets(connectorName).offsets().isEmpty();
    }

    private ConnectorOffsets getOffsets(String connectorName) throws IOException {
        getContext.reset();
        getContext.runAndEnsureExitCodeOk(connectorName);
        String output = getContext.output().toString();
        return mapper.readValue(output, ConnectorOffsets.class);
    }

}
