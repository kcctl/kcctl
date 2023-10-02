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

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
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
import picocli.CommandLine;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SkipIfConnectVersionIsOlderThan("3.6")
class DeleteOffsetsCommandTest extends IntegrationTest {

    private static final long OFFSET_AVAILABILITY_TIMEOUT_SECONDS = 30;


    private final ObjectMapper mapper = new ObjectMapper();

    @InjectCommandContext
    KcctlCommandContext<DeleteOffsetsCommand> deleteContext;

    @InjectCommandContext
    KcctlCommandContext<GetOffsetsCommand> getContext;

    @InjectCommandContext
    KcctlCommandContext<StopConnectorCommand> stopContext;

    @Test
    public void delete_offsets_of_running_connector_should_return_error() {

        registerTestConnector("delete-offsets-test1");

        await()
                .atMost(Duration.ofSeconds(OFFSET_AVAILABILITY_TIMEOUT_SECONDS))
                .until(() -> {
                    deleteContext.reset();

                    if (!isOffsetAvailable("delete-offsets-test1")) {
                        // No offsets have been committed yet; wait a little longer for an offset commit to complete
                        return false;
                    }

                    int exitCode = deleteContext.commandLine().execute("delete-offsets-test1");
                    String errorMessage = deleteContext.error().toString();

                    assertThat(exitCode).isEqualTo(CommandLine.ExitCode.SOFTWARE);
                    assertThat(errorMessage).contains("Connectors must be in the STOPPED state before their offsets can be modified. This can be done for the specified connector by issuing a 'PUT' request to the '/connectors/delete-offsets-test1/stop' endpoint" );
                    return true;
                });
    }

    @Test
    public void offsets_are_correctly_deleted() {

        registerTestConnector("delete-offsets-test1");

        await()
                .atMost(Duration.ofSeconds(OFFSET_AVAILABILITY_TIMEOUT_SECONDS))
                .until(() -> {
                    deleteContext.reset();

                    if (!isOffsetAvailable("delete-offsets-test1")) {
                        // No offsets have been committed yet; wait a little longer for an offset commit to complete
                        return false;
                    }

                    stopContext.runAndEnsureExitCodeOk("delete-offsets-test1");

                    int exitCode = deleteContext.commandLine().execute("delete-offsets-test1");
                    String output = deleteContext.output().toString();
                    ConnectorOffsets offsets = mapper.readValue(output, ConnectorOffsets.class);

                    assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
                    assertThat(offsets.offsets()).isNull();

                    return true;
                });
    }

    @Test
    public void offsets_are_correctly_deleted_multiple_connectors() {

        registerTestConnectors("delete-offsets-test1", "delete-offsets-test2");

        await()
                .atMost(Duration.ofSeconds(OFFSET_AVAILABILITY_TIMEOUT_SECONDS))
                .until(() -> {
                    deleteContext.reset();

                    if (!isOffsetAvailable("delete-offsets-test1") && !isOffsetAvailable("delete-offsets-test2")) {
                        // No offsets have been committed yet; wait a little longer for an offset commit to complete
                        return false;
                    }

                    stopContext.runAndEnsureExitCodeOk("delete-offsets-test1", "delete-offsets-test2");

                    int exitCode = deleteContext.commandLine().execute("delete-offsets-test1", "delete-offsets-test2");
                    String output = deleteContext.output().toString();
                    ConnectorOffsets offsets = mapper.readValue(output, ConnectorOffsets.class);

                    assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
                    assertThat(offsets.offsets()).isNull();

                    return true;
                });
    }

    private boolean isOffsetAvailable(String connectorName) throws IOException {
        getContext.reset();
        getContext.runAndEnsureExitCodeOk(connectorName);
        String output = getContext.output().toString();
        ConnectorOffsets offsets = mapper.readValue(output, ConnectorOffsets.class);

        return !offsets.offsets().isEmpty();
    }

}
