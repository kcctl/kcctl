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
package org.kcctl.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.ConnectException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kcctl.util.Colors;
import org.kcctl.util.ConfigurationContext;

import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayNameGeneration(ReplaceUnderscores.class)
class ExecutionExceptionHandlerTest {

    @TempDir
    File tempDir;

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
        void should_handle_unauthorized_errors() throws Exception {
            var handler = new ExecutionExceptionHandler(new ConfigurationContext().getCurrentContext());
            int exitCode = handler.handleExecutionException(
                    new KafkaConnectException("Unauthorized", 401), null, null);

            assertThat(exitCode).isEqualTo(CommandLine.ExitCode.SOFTWARE);
            assertThat(fakeSystemErr.toString())
                    .isEqualTo(errorPrintLnFormatted(
                            "The configured user is unauthorized to run this command."));
        }

        @Test
        void should_allow_uncaught_kc_exceptions_to_bubble_up() {
            var handler = new ExecutionExceptionHandler(new ConfigurationContext().getCurrentContext());
            assertThrows(KafkaConnectException.class, () -> handler.handleExecutionException(
                    new KafkaConnectException("Woa", 999), null, null));
        }

        @Test
        void should_allow_uncaught_exceptions_to_bubble_up() {
            var handler = new ExecutionExceptionHandler(new ConfigurationContext().getCurrentContext());
            assertThrows(Exception.class, () -> handler.handleExecutionException(
                    new Exception("Woa"), null, null));
        }

        @Test
        void should_handle_connect_exception() throws Exception {
            var handler = new ExecutionExceptionHandler(new ConfigurationContext().getCurrentContext());
            int exitCode = handler.handleExecutionException(
                    new ConnectException(), null, null);

            assertThat(exitCode).isEqualTo(CommandLine.ExitCode.SOFTWARE);
            assertThat(fakeSystemErr.toString())
                    .isEqualTo(errorPrintLnFormatted(
                            "Couldn't connect to Kafka Connect API at http://localhost:8083."));
        }
    }
}
