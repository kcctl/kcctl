/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import java.util.List;

import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "connector-name-completions", hidden = true)
public class ConnectorNamesCompletionCandidateCommand implements Runnable {

    private final ConfigurationContext context;

    @Spec
    private CommandSpec spec;

    @Inject
    public ConnectorNamesCompletionCandidateCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public ConnectorNamesCompletionCandidateCommand() {
        context = new ConfigurationContext();
    }

    @Override
    public void run() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        List<String> connectors = kafkaConnectApi.getConnectors();
        spec.commandLine().getOut().println(String.join(" ", connectors));
    }
}
