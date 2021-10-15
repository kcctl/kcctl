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
package org.moditect.kcctl.service;

import java.io.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.moditect.kcctl.command.KcCtlCommand;
import org.moditect.kcctl.util.Colors;

import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
class DeleteConnectorExceptionHandlerTest {

    @Nested
    class HandleExecutionException {
        // Thanks to https://stackoverflow.com/a/1119559
        private final ByteArrayOutputStream fakeSystemErr = new ByteArrayOutputStream();
        private final PrintStream originalSystemErr = System.err;

        @BeforeEach
        public void setupSystemErr() {
            System.setErr(new PrintStream(fakeSystemErr));
        }

        @AfterEach
        public void restoreSystemErr() {
            System.setErr(originalSystemErr);
        }

        public String errorPrintLnFormatted(String str) {
            return Colors.ANSI_RED + str + Colors.ANSI_RESET + System.getProperty("line.separator");
        }

        @Test
        void should_handle_not_found_errors()  {

            KcCtlCommand kcCtlCommand = new KcCtlCommand();
            CommandLine cmd = new CommandLine(kcCtlCommand);
            String connectorName = "connectorName";
            int exitCode = cmd.execute("delete", connectorName);
            assertThat(exitCode).isEqualTo(CommandLine.ExitCode.SOFTWARE);
            assertThat(fakeSystemErr.toString()).contains("connector " + connectorName + " not found");

        }
    }
}
