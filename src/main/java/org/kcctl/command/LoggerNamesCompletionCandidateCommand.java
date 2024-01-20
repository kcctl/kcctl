/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import java.util.Set;

import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;

import static picocli.CommandLine.*;

@Command(name = "logger-name-completions", hidden = true)
public class LoggerNamesCompletionCandidateCommand implements Runnable {

    @Spec
    private CommandSpec spec;

    private final ConfigurationContext context;

    @Inject
    public LoggerNamesCompletionCandidateCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public LoggerNamesCompletionCandidateCommand() {
        context = new ConfigurationContext();
    }

    @Override
    public void run() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        Set<String> loggers = kafkaConnectApi.getLoggers().keySet();
        spec.commandLine().getOut().println(String.join(" ", loggers));
    }
}
