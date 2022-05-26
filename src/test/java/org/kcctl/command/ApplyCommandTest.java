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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.kcctl.IntegrationTest;
import org.kcctl.IntegrationTestProfile;
import org.kcctl.support.InjectCommandContext;
import org.kcctl.support.KcctlCommandContext;

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
    public void should_create_two_connectors() {
        var path = Paths.get("src", "test", "resources", "local-file-source.json");
        var path2 = Paths.get("src", "test", "resources", "local-file-source-2.json");
        int exitCode = context.commandLine().execute("-f", path.toAbsolutePath().toString(), "-f", path2.toAbsolutePath().toString());
        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(context.output().toString()).contains("Created connector local-file-source", "Created connector local-file-source-2");

        kafkaConnect.ensureConnectorRegistered("local-file-source");
        kafkaConnect.ensureConnectorRegistered("local-file-source-2");
        kafkaConnect.ensureConnectorState("local-file-source", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorState("local-file-source-2", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorTaskState("local-file-source", 0, Connector.State.RUNNING);
        kafkaConnect.ensureConnectorTaskState("local-file-source-2", 0, Connector.State.RUNNING);
    }

    @Test
    public void should_create_connector_from_stdin() throws IOException {
        var path = Paths.get("src", "test", "resources", "local-file-source.json");
        InputStream fakeIn = new ByteArrayInputStream(Files.readAllBytes(path));
        System.setIn(fakeIn);

        int exitCode = context.commandLine().execute("-f", "-");
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

    @Test
    public void do_not_allow_n_with_multiple_f() {
        int exitCode = context.commandLine().execute("-f", "test", "-f", "test2", "-n", "test name");
        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.USAGE);
    }

    @Test
    public void dont_apply_if_files_wrong() {
        var path = Paths.get("src", "test", "resources", "local-file-source.json");
        var wrongPath = Paths.get("src", "test", "resources", "dont_exists.json");

        int exitCode = context.commandLine().execute("-f", path.toAbsolutePath().toString(), "-f", wrongPath.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(context.output().toString()).doesNotContain("Created connector");
    }

    @Test
    public void fail_on_first_error() {
        var pathBad = Paths.get("src", "test", "resources", "local-file-source-bad.json");
        var path = Paths.get("src", "test", "resources", "local-file-source.json");

        int exitCode = context.commandLine().execute("-f", pathBad.toAbsolutePath().toString(), "-f", path.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.SOFTWARE);
        assertThat(context.output().toString()).doesNotContain("Created connector");
    }

    @Test
    public void should_create_connector_successfuly_with_parametric_json_from_stdin() throws IOException {
        var path = Paths.get("src", "test", "resources", "local-file-source-parameters.json");
        InputStream fakeIn = new ByteArrayInputStream(Files.readAllBytes(path));
        System.setIn(fakeIn);

        String expectedTopicName = "my-topic";
        String expectedFilePath = "/tmp/test-2";
        String expectedPassword = "pass";
        String parameter = expectedTopicName + " " + expectedFilePath + " " + expectedPassword;

        int exitCode = context.commandLine().execute("-f", "-", "-ca", parameter);
        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(context.output().toString().trim()).isEqualTo("Created connector local-file-source-parameters");

        kafkaConnect.ensureConnectorRegistered("local-file-source-parameters");
        kafkaConnect.ensureConnectorState("local-file-source-parameters", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorTaskState("local-file-source-parameters", 0, Connector.State.RUNNING);
        
        String actualTopicName = kafkaConnect.getConnectorConfigProperty("local-file-source-parameters", "topic");
        String actualFilePath = kafkaConnect.getConnectorConfigProperty("local-file-source-parameters", "file");
        String actualPassword = kafkaConnect.getConnectorConfigProperty("local-file-source-parameters", "dbpassword");

        assertThat(actualTopicName).isEqualTo(expectedTopicName);
        assertThat(actualFilePath).isEqualTo(expectedFilePath);
        assertThat(actualPassword).isEqualTo(expectedPassword);
    }

    @Test
    public void should_update_connector_successfuly_with_parametric_json() throws IOException {
        var path = Paths.get("src", "test", "resources", "local-file-source-parameters.json");

        String expectedTopicName = "my-topic";
        String expectedFilePath = "/tmp/test-2";
        String expectedPassword = "pass";
        String parameter = expectedTopicName + " " + expectedFilePath + " " + expectedPassword;

        int exitCode = context.commandLine().execute("-f", path.toAbsolutePath().toString(), "-ca", parameter);
        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(context.output().toString().trim()).isEqualTo("Created connector local-file-source-parameters");

        String expectedUpdateTopicName = "my-topic3";
        parameter = expectedUpdateTopicName + " " + expectedFilePath + " " + expectedPassword;

        exitCode = context.commandLine().execute("-f", path.toAbsolutePath().toString(), "-ca", parameter);
        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);

        kafkaConnect.ensureConnectorRegistered("local-file-source-parameters");
        kafkaConnect.ensureConnectorState("local-file-source-parameters", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorTaskState("local-file-source-parameters", 0, Connector.State.RUNNING);

        String actualUpdateTopicName = kafkaConnect.getConnectorConfigProperty("local-file-source-parameters", "topic");

        assertThat(actualUpdateTopicName).isEqualTo(expectedUpdateTopicName);
    }
}
