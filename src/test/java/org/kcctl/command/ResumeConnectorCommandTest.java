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

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
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
@DisplayNameGeneration(ReplaceUnderscores.class)
class ResumeConnectorCommandTest extends IntegrationTest {

    @InjectCommandContext
    KcctlCommandContext<ResumeConnectorCommand> context;

    @Test
    public void should_resume_connector() {
        registerTestConnector("test1");
        registerTestConnector("test2");
        registerTestConnector("test3");

        int exitCode = context.commandLine().execute("test1", "test2");
        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(context.output().toString()).contains("Resumed connector test1", "Resumed connector test2");
        assertThat(context.output().toString()).doesNotContain("Resumed connector test3");

        kafkaConnect.ensureConnectorState("test1", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorState("test2", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorState("test3", Connector.State.RUNNING);
    }

    @Test
    public void should_resume_connector_with_regexp() {
        registerTestConnector("match-1-test");
        registerTestConnector("match-2-test");
        registerTestConnector("nomatch-3-test");

        int exitCode = context.commandLine().execute("--reg-exp", "match-\\d-test");

        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(context.output().toString()).contains("Resumed connector match-1-test", "Resumed connector match-2-test");
        assertThat(context.output().toString()).doesNotContain("Resumed connector nomatch-3-test");

        kafkaConnect.ensureConnectorState("match-1-test", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorState("match-2-test", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorState("nomatch-3-test", Connector.State.RUNNING);
    }

    @Test
    public void should_resume_only_once() {
        registerTestConnector("test1");

        int exitCode = context.commandLine().execute("test1", "test1", "test1");

        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(context.output().toString()).containsOnlyOnce("Resumed connector test1");

        kafkaConnect.ensureConnectorState("test1", Connector.State.RUNNING);
    }

    @Test
    public void should_resume_only_once_with_regexp() {
        registerTestConnector("match-1-test");
        registerTestConnector("match-2-test");
        registerTestConnector("nomatch-3-test");

        int exitCode = context.commandLine().execute("--reg-exp", "match-\\d-test", "match-.*", "match-1-test");

        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(context.output().toString()).containsOnlyOnce("Resumed connector match-1-test");
        assertThat(context.output().toString()).containsOnlyOnce("Resumed connector match-2-test");
        assertThat(context.output().toString()).doesNotContain("Resumed connector nomatch-3-test");

        kafkaConnect.ensureConnectorState("match-1-test", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorState("match-2-test", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorState("nomatch-3-test", Connector.State.RUNNING);
    }
}
