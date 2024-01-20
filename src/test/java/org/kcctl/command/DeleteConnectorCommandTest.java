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

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class DeleteConnectorCommandTest extends IntegrationTest {

    @InjectCommandContext
    KcctlCommandContext<DeleteConnectorCommand> context;

    @Test
    public void should_delete_connector() {
        registerTestConnectors("test1", "test2", "test3");

        context.runAndEnsureExitCodeOk("test1", "test2");

        assertThat(context.output().toString()).contains("Deleted connector test1", "Deleted connector test2");
        assertThat(context.output().toString()).doesNotContain("Deleted connector test3");
    }

    @Test
    public void should_delete_connector_with_regexp() {
        registerTestConnectors("match-1-test", "match-2-test", "nomatch-3-test");

        context.runAndEnsureExitCodeOk("--reg-exp", "match-\\d-test");

        assertThat(context.output().toString()).contains("Deleted connector match-1-test", "Deleted connector match-2-test");
        assertThat(context.output().toString()).doesNotContain("Deleted connector nomatch-3-test");
    }

    @Test
    public void should_delete_only_once() {
        registerTestConnector("test1");

        context.runAndEnsureExitCodeOk("test1", "test1", "test1");

        assertThat(context.output().toString()).containsOnlyOnce("Deleted connector test1");
    }

    @Test
    public void should_delete_only_once_with_regexp() {
        registerTestConnectors("match-1-test", "match-2-test", "nomatch-3-test");

        context.runAndEnsureExitCodeOk("--reg-exp", "match-\\d-test", "match-.*", "match-1-test");

        assertThat(context.output().toString()).containsOnlyOnce("Deleted connector match-1-test");
        assertThat(context.output().toString()).containsOnlyOnce("Deleted connector match-2-test");
        assertThat(context.output().toString()).doesNotContain("Deleted connector nomatch-3-test");
    }
}
