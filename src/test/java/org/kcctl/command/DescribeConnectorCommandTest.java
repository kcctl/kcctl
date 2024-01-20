/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
class DescribeConnectorCommandTest extends IntegrationTest {

    @InjectCommandContext
    KcctlCommandContext<DescribeConnectorCommand> context;

    @Test
    public void should_describe_connector() {
        registerTestConnectors("test1", "test2");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);

        context.runAndEnsureExitCodeOk("test1", "test2");
        System.setOut(old);

        assertThat(baos.toString()).contains(HEARTBEAT_TOPIC);
    }

    @Test
    public void should_describe_connector_with_json() {
        registerTestConnector("test1");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);

        context.runAndEnsureExitCodeOk("test1", "--output-format", "json");
        System.setOut(old);

        assertThat(baos.toString()).contains(HEARTBEAT_TOPIC);
        assertThat(baos.toString()).contains("\"name\" : \"test1\",");
    }
}
