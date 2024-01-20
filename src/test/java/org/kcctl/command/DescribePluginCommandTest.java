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
import org.kcctl.support.SkipIfConnectVersionIsOlderThan;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kcctl.util.Colors.ANSI_RESET;
import static org.kcctl.util.Colors.ANSI_WHITE_BOLD;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
@SkipIfConnectVersionIsOlderThan("3.2")
class DescribePluginCommandTest extends IntegrationTest {

    @InjectCommandContext
    KcctlCommandContext<DescribePluginCommand> context;

    @Test
    public void should_describe_plugin() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);
        context.runAndEnsureExitCodeOk("org.apache.kafka.connect.mirror.MirrorCheckpointConnector");
        System.setOut(old);

        assertThat(baos.toString()).contains(
                ANSI_WHITE_BOLD + "Name" + ANSI_RESET + ":           connector.class\n" +
                        ANSI_WHITE_BOLD + "Type" + ANSI_RESET + ":           STRING\n" +
                        ANSI_WHITE_BOLD + "Required" + ANSI_RESET + ":       true\n" +
                        ANSI_WHITE_BOLD + "Default" + ANSI_RESET + ":        null\n" +
                        ANSI_WHITE_BOLD + "Documentation" + ANSI_RESET + ":");
    }

    @Test
    public void should_describe_plugin_with_dollar() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);
        context.runAndEnsureExitCodeOk("org.apache.kafka.connect.transforms.ExtractField$Key");
        System.setOut(old);

        assertThat(baos.toString()).contains(
                ANSI_WHITE_BOLD + "Name" + ANSI_RESET + ":           field\n" +
                        ANSI_WHITE_BOLD + "Type" + ANSI_RESET + ":           STRING\n" +
                        ANSI_WHITE_BOLD + "Required" + ANSI_RESET + ":       true\n" +
                        ANSI_WHITE_BOLD + "Default" + ANSI_RESET + ":        null\n" +
                        ANSI_WHITE_BOLD + "Documentation" + ANSI_RESET + ":");
    }
}
