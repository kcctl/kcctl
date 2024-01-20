/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class PauseConnectorCommandTest extends IntegrationTest {

    @InjectCommandContext
    KcctlCommandContext<PauseConnectorCommand> context;

    @Test
    public void should_pause_connector() {
        registerTestConnectors("test1", "test2", "test3");

        context.runAndEnsureExitCodeOk("test1", "test2");
        assertThat(context.output().toString()).contains("Paused connector test1", "Paused connector test2");
        assertThat(context.output().toString()).doesNotContain("Paused connector test3");

        kafkaConnect.ensureConnectorState("test1", Connector.State.PAUSED);
        kafkaConnect.ensureConnectorState("test2", Connector.State.PAUSED);
        kafkaConnect.ensureConnectorState("test3", Connector.State.RUNNING);
    }

    @Test
    public void should_pause_connector_with_regexp() {
        registerTestConnectors("match-1-test", "match-2-test", "nomatch-3-test");

        context.runAndEnsureExitCodeOk("--reg-exp", "match-\\d-test");
        assertThat(context.output().toString()).contains("Paused connector match-1-test", "Paused connector match-2-test");
        assertThat(context.output().toString()).doesNotContain("Paused connector nomatch-3-test");

        kafkaConnect.ensureConnectorState("match-1-test", Connector.State.PAUSED);
        kafkaConnect.ensureConnectorState("match-2-test", Connector.State.PAUSED);
        kafkaConnect.ensureConnectorState("nomatch-3-test", Connector.State.RUNNING);
    }

    @Test
    public void should_pause_only_once() {
        registerTestConnector("test1");

        context.runAndEnsureExitCodeOk("test1", "test1", "test1");
        assertThat(context.output().toString()).containsOnlyOnce("Paused connector test1");

        kafkaConnect.ensureConnectorState("test1", Connector.State.PAUSED);
    }

    @Test
    public void should_pause_only_once_with_regexp() {
        registerTestConnectors("match-1-test", "match-2-test", "nomatch-3-test");

        context.runAndEnsureExitCodeOk("--reg-exp", "match-\\d-test", "match-.*", "match-1-test");
        assertThat(context.output().toString()).containsOnlyOnce("Paused connector match-1-test");
        assertThat(context.output().toString()).containsOnlyOnce("Paused connector match-2-test");
        assertThat(context.output().toString()).doesNotContain("Paused connector nomatch-3-test");

        kafkaConnect.ensureConnectorState("match-1-test", Connector.State.PAUSED);
        kafkaConnect.ensureConnectorState("match-2-test", Connector.State.PAUSED);
        kafkaConnect.ensureConnectorState("nomatch-3-test", Connector.State.RUNNING);
    }
}
