/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import java.util.Set;
import java.util.concurrent.Callable;

import jakarta.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.completion.ConnectorNameCompletions;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.service.KafkaConnectException;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Connectors;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "connector", description = "Deletes specified connectors")
public class DeleteConnectorCommand implements Callable<Integer> {

    @CommandLine.Mixin
    HelpMixin help;
    @CommandLine.Option(names = { "-e", "--reg-exp" }, description = "use CONNECTOR NAME(s) as regexp pattern(s) to apply on all connectors")
    boolean regexpMode = false;

    @Parameters(paramLabel = "CONNECTOR NAME", description = "Name of the connector", completionCandidates = ConnectorNameCompletions.class)
    Set<String> names = Set.of();

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    private final ConfigurationContext context;

    @Inject
    public DeleteConnectorCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public DeleteConnectorCommand() {
        context = new ConfigurationContext();
    }

    @Override
    public Integer call() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        try {
            Set<String> selectedConnector = Connectors.getSelectedConnectors(kafkaConnectApi, names, regexpMode);
            for (String connectorToDelete : selectedConnector) {
                kafkaConnectApi.deleteConnector(connectorToDelete);
                spec.commandLine().getOut().println("Deleted connector " + connectorToDelete);
            }
        }
        catch (KafkaConnectException kce) {
            if (kce.getErrorCode() == HttpStatus.SC_NOT_FOUND) {
                spec.commandLine().getOut().println(kce.getMessage());
            }
            else {
                spec.commandLine().getOut().println(kce);
            }
            return 1;
        }

        return 0;
    }
}
