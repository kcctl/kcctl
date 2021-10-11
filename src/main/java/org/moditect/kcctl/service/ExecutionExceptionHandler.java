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

import org.apache.http.HttpStatus;
import org.moditect.kcctl.util.Colors;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

public class ExecutionExceptionHandler implements IExecutionExceptionHandler {

    public static class ExitCodeErrorMessagePair {
        private int exitCode;
        private String errorMessage;

        public ExitCodeErrorMessagePair(int exitCode, String errorMessage) {
            this.exitCode = exitCode;
            this.errorMessage = errorMessage;
        }

        public int getExitCode() {
            return this.exitCode;
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }
    }

    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) throws Exception {
        var exitCodeErrorMessagePair = loadMessageAndExitCode(ex);
        if (exitCodeErrorMessagePair == null) {
            throw ex;
        }

        System.err.println(Colors.ANSI_RED + exitCodeErrorMessagePair.getErrorMessage() + Colors.ANSI_RESET);
        return exitCodeErrorMessagePair.getExitCode();
    }

    public static ExitCodeErrorMessagePair loadMessageAndExitCode(Exception ex) {
        if (ex instanceof KafkaConnectException) {
            var kafkaConnectException = (KafkaConnectException) ex;
            switch (kafkaConnectException.getErrorCode()) {
                case HttpStatus.SC_UNAUTHORIZED: {
                    return new ExitCodeErrorMessagePair(
                            CommandLine.ExitCode.SOFTWARE,
                            "The configured user is unauthorized to run this command.");
                }
            }
        }
        return null;
    }
}
