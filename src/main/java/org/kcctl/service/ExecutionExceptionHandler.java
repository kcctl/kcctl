/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.service;

import java.net.ConnectException;

import org.kcctl.util.Colors;

import jakarta.ws.rs.core.Response.Status;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

public class ExecutionExceptionHandler implements IExecutionExceptionHandler {

    public record ExitCodeErrorMessagePair(int exitCode, String errorMessage) {
    }

    private final Context context;

    public ExecutionExceptionHandler(Context context) {
        this.context = context;
    }

    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) throws Exception {
        var exitCodeErrorMessagePair = loadMessageAndExitCode(ex);
        if (exitCodeErrorMessagePair == null) {
            throw ex;
        }

        System.err.println(Colors.ANSI_RED + exitCodeErrorMessagePair.errorMessage() + Colors.ANSI_RESET);
        return exitCodeErrorMessagePair.exitCode();
    }

    private ExitCodeErrorMessagePair loadMessageAndExitCode(Exception ex) {
        Throwable rootCause = getRootCause(ex);

        if (rootCause instanceof ConnectException) {
            return new ExitCodeErrorMessagePair(
                    CommandLine.ExitCode.SOFTWARE,
                    "Couldn't connect to Kafka Connect API at %s.".formatted(context.getCluster()));
        }

        if (ex instanceof KafkaConnectException) {
            var kafkaConnectException = (KafkaConnectException) ex;
            switch (kafkaConnectException.getHttpStatus()) {
                case Status.UNAUTHORIZED: {
                    return new ExitCodeErrorMessagePair(
                            CommandLine.ExitCode.SOFTWARE,
                            "The configured user is unauthorized to run this command.");
                }
            }
        }

        return null;
    }

    private static Throwable getRootCause(Throwable ex) {
        Throwable parent;

        while ((parent = ex.getCause()) != null) {
            ex = parent;
        }

        return ex;
    }
}
