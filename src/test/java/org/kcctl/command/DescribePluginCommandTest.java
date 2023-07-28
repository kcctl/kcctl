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
import static org.kcctl.util.Colors.ANSI_RESET;
import static org.kcctl.util.Colors.ANSI_WHITE_BOLD;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
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
