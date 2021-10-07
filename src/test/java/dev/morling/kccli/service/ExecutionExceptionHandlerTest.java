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
package dev.morling.kccli.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.morling.kccli.util.Colors;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

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
        void should_handle_unauthorized_errors() throws IOException {
            var handler = new ExecutionExceptionHandler();
            int exitCode = handler.handleExecutionException(
                    new KafkaConnectException("Unauthorized", 401), null, null);

            assertThat(exitCode).isEqualTo(CommandLine.ExitCode.SOFTWARE);
            assertThat(fakeSystemErr.toString())
                    .isEqualTo(errorPrintLnFormatted(
                            "Kafka Connect returned an error indicating the configured user is unauthorized."));
        }

        @Test
        void should_use_default_error_message_and_code_for_other_kc_exceptions() throws IOException {
            var handler = new ExecutionExceptionHandler();
            int exitCode = handler.handleExecutionException(
                    new KafkaConnectException("Woa", 999), null, null);

            assertThat(exitCode).isEqualTo(CommandLine.ExitCode.SOFTWARE);
            assertThat(fakeSystemErr.toString())
                    .isEqualTo(errorPrintLnFormatted("An error occured."));
        }

        @Test
        void should_use_default_error_message_and_code_for_non_kc_exceptions() throws IOException {
            var handler = new ExecutionExceptionHandler();
            int exitCode = handler.handleExecutionException(
                    new Exception("Woa"), null, null);

            assertThat(exitCode).isEqualTo(CommandLine.ExitCode.SOFTWARE);
            assertThat(fakeSystemErr.toString())
                    .isEqualTo(errorPrintLnFormatted("An error occured."));
        }
    }
}
