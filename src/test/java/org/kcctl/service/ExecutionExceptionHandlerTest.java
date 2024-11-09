/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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

import jakarta.ws.rs.core.Response.Status;
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
            return Colors.ANSI_RED + str + Colors.ANSI_RESET + System.lineSeparator();
        }

        @Test
        void should_handle_unauthorized_errors() throws Exception {
            var handler = new ExecutionExceptionHandler(new ConfigurationContext().getCurrentContext());
            int exitCode = handler.handleExecutionException(
                    new KafkaConnectException("Unauthorized", Status.UNAUTHORIZED), null, null);

            assertThat(exitCode).isEqualTo(CommandLine.ExitCode.SOFTWARE);
            assertThat(fakeSystemErr.toString())
                    .isEqualTo(errorPrintLnFormatted(
                            "The configured user is unauthorized to run this command."));
        }

        @Test
        void should_allow_uncaught_kc_exceptions_to_bubble_up() {
            var handler = new ExecutionExceptionHandler(new ConfigurationContext().getCurrentContext());
            assertThrows(KafkaConnectException.class, () -> handler.handleExecutionException(
                    new KafkaConnectException("Woa", Status.INTERNAL_SERVER_ERROR), null, null));
        }

        @Test
        void should_allow_uncaught_exceptions_to_bubble_up() {
            var handler = new ExecutionExceptionHandler(new ConfigurationContext().getCurrentContext());
            assertThrows(Exception.class, () -> handler.handleExecutionException(
                    new Exception("Woa"), null, null));
        }

        @Test
        void should_handle_connect_exception() throws Exception {
            var handler = new ExecutionExceptionHandler(Context.defaultContext());
            int exitCode = handler.handleExecutionException(
                    new ConnectException(), null, null);

            assertThat(exitCode).isEqualTo(CommandLine.ExitCode.SOFTWARE);
            assertThat(fakeSystemErr.toString())
                    .isEqualTo(errorPrintLnFormatted(
                            "Couldn't connect to Kafka Connect API at http://localhost:8083."));
        }
    }
}
