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

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class GetLoggerCommandTest extends IntegrationTest {

    @InjectCommandContext
    KcctlCommandContext<GetLoggerCommand> context;

    @Test
    public void should_print_all_loggers() {
        int exitCode = context.commandLine().execute("ALL");
        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(context.output().toString().trim().lines())
                .map(String::trim)
                .containsExactly(
                        "LOGGER            LEVEL",
                        "org.reflections   \u001B[31mERROR\u001B[0m",
                        "root              \u001B[32mINFO\u001B[0m");
    }

    @Test
    public void should_print_root_logger() {
        int exitCode = context.commandLine().execute("root");
        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(context.output().toString().trim().lines())
                .map(String::trim)
                .containsExactly(
                        "LOGGER   LEVEL",
                        "root    \u001B[32mINFO\u001B[0m");
    }

    @Test
    public void should_print_reflections_logger() {
        int exitCode = context.commandLine().execute("org.reflections");
        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(context.output().toString().trim().lines())
                .map(String::trim)
                .containsExactly(
                        "LOGGER            LEVEL",
                        "org.reflections  \u001B[31mERROR\u001B[0m");
    }
}
