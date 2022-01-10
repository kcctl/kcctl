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

import java.nio.file.Paths;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.kcctl.IntegrationTest;
import org.kcctl.IntegrationTestProfile;
import org.kcctl.support.*;

import io.debezium.testing.testcontainers.Connector;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ApplyCommandTest extends IntegrationTest {

    @InjectCommandContext
    KcctlCommandContext<ApplyCommand> context;

    @Test
    public void should_create_connector() {
        var path = Paths.get("src", "test", "resources", "local-file-source.json");
        int exitCode = context.commandLine().execute("-f", path.toAbsolutePath().toString());
        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(context.output().toString().trim()).isEqualTo("Created connector local-file-source");

        kafkaConnect.ensureConnectorRegistered("local-file-source");
        kafkaConnect.ensureConnectorState("local-file-source", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorTaskState("local-file-source", 0, Connector.State.RUNNING);
    }

    @Test
    public void should_update_connector() {
        var path = Paths.get("src", "test", "resources", "local-file-source.json");
        int exitCode = context.commandLine().execute("-f", path.toAbsolutePath().toString());
        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(context.output().toString().trim()).isEqualTo("Created connector local-file-source");

        path = Paths.get("src", "test", "resources", "local-file-source-update.json");
        exitCode = context.commandLine().execute("-f", path.toAbsolutePath().toString());
        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(context.output().toString()).isEqualTo("""
                Created connector local-file-source
                Updated connector local-file-source
                """);

        kafkaConnect.ensureConnectorRegistered("local-file-source");
        kafkaConnect.ensureConnectorState("local-file-source", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorTaskState("local-file-source", 0, Connector.State.RUNNING);
    }
}