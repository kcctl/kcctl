/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import java.util.List;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.kcctl.IntegrationTest;
import org.kcctl.IntegrationTestProfile;
import org.kcctl.support.InjectCommandContext;
import org.kcctl.support.KcctlCommandContext;
import org.kcctl.support.SkipIfConnectVersionIsOlderThan;

import io.debezium.testing.testcontainers.Connector;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class RestartConnectorCommandTest extends IntegrationTest {

    @InjectCommandContext
    KcctlCommandContext<RestartConnectorCommand> context;

    @Test
    public void should_restart_connector() {
        registerTestConnectors("test1", "test2", "test3");

        context.runAndEnsureExitCodeOk("test1", "test2");
        assertThat(context.output().toString()).contains("Restarted connector test1", "Restarted connector test2");
        assertThat(context.output().toString()).doesNotContain("Restarted connector test3");

        kafkaConnect.ensureConnectorState("test1", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorState("test2", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorState("test3", Connector.State.RUNNING);
    }

    @Test
    public void should_restart_connector_with_regexp() {
        registerTestConnectors("match-1-test", "match-2-test", "nomatch-3-test");

        context.runAndEnsureExitCodeOk("--reg-exp", "match-\\d-test");
        assertThat(context.output().toString()).contains("Restarted connector match-1-test", "Restarted connector match-2-test");
        assertThat(context.output().toString()).doesNotContain("Restarted connector nomatch-3-test");

        kafkaConnect.ensureConnectorState("match-1-test", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorState("match-2-test", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorState("nomatch-3-test", Connector.State.RUNNING);
    }

    @Test
    public void should_restart_only_once() {
        registerTestConnector("test1");

        context.runAndEnsureExitCodeOk("test1", "test1", "test1");
        assertThat(context.output().toString()).containsOnlyOnce("Restarted connector test1");

        kafkaConnect.ensureConnectorState("test1", Connector.State.RUNNING);
    }

    @Test
    public void should_restart_only_once_with_regexp() {
        registerTestConnectors("match-1-test", "match-2-test", "nomatch-3-test");

        context.runAndEnsureExitCodeOk("--reg-exp", "match-\\d-test", "match-.*", "match-1-test");
        assertThat(context.output().toString()).containsOnlyOnce("Restarted connector match-1-test");
        assertThat(context.output().toString()).containsOnlyOnce("Restarted connector match-2-test");
        assertThat(context.output().toString()).doesNotContain("Restarted connector nomatch-3-test");

        kafkaConnect.ensureConnectorState("match-1-test", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorState("match-2-test", Connector.State.RUNNING);
        kafkaConnect.ensureConnectorState("nomatch-3-test", Connector.State.RUNNING);
    }

    @Test
    @SkipIfConnectVersionIsOlderThan("3.0")
    public void should_restart_connectors_and_all_tasks() {
        registerTestConnectors("test1", "test2", "test3");

        for (String connector : List.of("test1", "test2")) {
            kafkaConnect.ensureConnectorState(connector, Connector.State.RUNNING);
            kafkaConnect.ensureConnectorTaskState(connector, 0, Connector.State.RUNNING);
        }

        context.runAndEnsureExitCodeOk("-t", "all", "test1", "test2");
        assertThat(context.output().toString()).contains("Restarted connector test1 and 1 task(s)", "Restarted connector test2 and 1 task(s)");
        assertThat(context.output().toString()).doesNotContain("Restarted connector test3");
    }

    @Test
    @SkipIfConnectVersionIsOlderThan("3.0")
    public void should_restart_connector_and_failed_tasks() {
        registerTestConnectors("test1", "test2", "test3");

        for (String connector : List.of("test1", "test2")) {
            kafkaConnect.ensureConnectorState(connector, Connector.State.RUNNING);
            kafkaConnect.ensureConnectorTaskState(connector, 0, Connector.State.RUNNING);
        }

        context.runAndEnsureExitCodeOk("-t", "failed", "test1", "test2");
        assertThat(context.output().toString()).contains("Restarted connector test1 and 0 task(s)", "Restarted connector test2 and 0 task(s)");
        assertThat(context.output().toString()).doesNotContain("Restarted connector test3");
    }

}
