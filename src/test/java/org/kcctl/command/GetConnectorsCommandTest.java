/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
class GetConnectorsCommandTest extends IntegrationTest {

    @InjectCommandContext
    KcctlCommandContext<GetConnectorsCommand> context;

    @Test
    public void should_print_empty_connectors_() {
        context.runAndEnsureExitCodeOk();
        assertThat(context.output().toString().trim()).isEqualTo(
                "NAME   TYPE   STATE   TASKS");
    }

    @Test
    public void should_print_registered_connectors() {
        registerTestConnectors("test1", "test2");
        for (String connector : List.of("test1", "test2")) {
            kafkaConnect.ensureConnectorState(connector, Connector.State.RUNNING);
            kafkaConnect.ensureConnectorTaskState(connector, 0, Connector.State.RUNNING);
        }

        Pattern singleTaskPattern = Pattern.compile(".*[0-9]+\\:\\s+(.*\\:[0-9]+\\s+).*");

        context.runAndEnsureExitCodeOk();
        assertThat(context.output().toString().trim().lines().map(m -> {
            String ret = m;
            Matcher matcher = singleTaskPattern.matcher(ret);
            if (matcher.matches()) {
                String workId = matcher.group(1);
                ret = ret.replace(workId, "");
            }
            return ret;
        }))
                .map(String::trim)
                .containsExactly(
                        "NAME    TYPE     STATE     TASKS",
                        "test1   source   \u001B[32mRUNNING\u001B[0m   0: \u001B[32mRUNNING\u001B[0m",
                        "test2   source   \u001B[32mRUNNING\u001B[0m   0: \u001B[32mRUNNING\u001B[0m");
    }
}
