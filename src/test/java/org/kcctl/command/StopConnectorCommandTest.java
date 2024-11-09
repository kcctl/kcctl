/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.kcctl.IntegrationTest;
import org.kcctl.IntegrationTestProfile;
import org.kcctl.support.InjectCommandContext;
import org.kcctl.support.KcctlCommandContext;
import org.kcctl.support.SkipIfConnectVersionIsOlderThan;

import io.debezium.testing.testcontainers.Connector;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SkipIfConnectVersionIsOlderThan("3.5")
class StopConnectorCommandTest extends IntegrationTest {

    @InjectCommandContext
    KcctlCommandContext<StopConnectorCommand> context;

    @Test
    public void should_stop_connector() {
        registerTestConnectors("test1", "test2", "test3");
        // Make sure that the connectors actually start before we try to stop them
        ensureConnectorsRunning("test1", "test2", "test3");

        // Then stop some but not all of the connectors
        context.runAndEnsureExitCodeOk("test1", "test2");
        assertThat(context.output().toString()).contains("Stopped connector test1", "Stopped connector test2");
        assertThat(context.output().toString()).doesNotContain("Stopped connector test3");

        // Ensure that those connectors are actually stopped
        ensureConnectorsStopped("test1", "test2");
        // And ensure that the third connector is still running
        ensureConnectorsRunning("test3");
    }

    @Test
    public void should_stop_connector_with_regexp() {
        registerTestConnectors("match-1-test", "match-2-test", "nomatch-3-test");
        // Make sure that the connectors actually start before we try to stop them
        ensureConnectorsRunning("match-1-test", "match-2-test", "nomatch-3-test");

        // Then stop some but not all of the connectors
        context.runAndEnsureExitCodeOk("--reg-exp", "match-\\d-test");
        assertThat(context.output().toString()).contains("Stopped connector match-1-test", "Stopped connector match-2-test");
        assertThat(context.output().toString()).doesNotContain("Stopped connector nomatch-3-test");

        // Ensure that those connectors are actually stopped
        ensureConnectorsStopped("match-1-test", "match-2-test");
        // And ensure that the third connector is still running
        ensureConnectorsRunning("nomatch-3-test");
    }

    @Test
    public void should_stop_only_once() {
        registerTestConnector("test1");
        ensureConnectorsRunning("test1");

        context.runAndEnsureExitCodeOk("test1", "test1", "test1");
        assertThat(context.output().toString()).containsOnlyOnce("Stopped connector test1");

        ensureConnectorsStopped("test1");
    }

    @Test
    public void should_stop_only_once_with_regexp() {
        registerTestConnectors("match-1-test", "match-2-test", "nomatch-3-test");
        // Make sure that the connectors actually start before we try to stop them
        ensureConnectorsRunning("match-1-test", "match-2-test", "nomatch-3-test");

        // Then stop some but not all of the connectors
        context.runAndEnsureExitCodeOk("--reg-exp", "match-\\d-test", "match-.*", "match-1-test");
        assertThat(context.output().toString()).containsOnlyOnce("Stopped connector match-1-test");
        assertThat(context.output().toString()).containsOnlyOnce("Stopped connector match-2-test");
        assertThat(context.output().toString()).doesNotContain("Stopped connector nomatch-3-test");

        // Ensure that those connectors are actually stopped
        ensureConnectorsStopped("match-1-test", "match-2-test");
        // And ensure that the third connector is still running
        ensureConnectorsRunning("nomatch-3-test");
    }

    private void ensureConnectorsRunning(String... connectors) {
        for (String connector : connectors) {
            // Make sure the connector and one task have actually started
            kafkaConnect.ensureConnectorState(connector, Connector.State.RUNNING);
            kafkaConnect.ensureConnectorTaskState(connector, 0, Connector.State.RUNNING);
        }
    }

    private void ensureConnectorsStopped(String... connectors) {
        for (String connector : connectors) {
            kafkaConnect.ensureConnectorState(connector, Connector.State.STOPPED);
        }
    }
}
