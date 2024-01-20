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
import org.kcctl.completion.ConnectorNameCompletions;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Connectors;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "connector", aliases = "connectors", description = "Pauses the specified connector(s)")
public class PauseConnectorCommand implements Runnable {

    @CommandLine.Mixin
    HelpMixin help;

    @CommandLine.Option(names = { "-e", "--reg-exp" }, description = "use NAME(s) as regexp pattern(s) to use on all connectors")
    boolean regexpMode = false;

    @Parameters(paramLabel = "NAME", description = "Name(s) of the connector(s) (e.g. 'my-connector').", completionCandidates = ConnectorNameCompletions.class)
    Set<String> names = Set.of();

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    private final ConfigurationContext context;

    @Inject
    public PauseConnectorCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public PauseConnectorCommand() {
        context = new ConfigurationContext();
    }

    @Override
    public void run() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        Set<String> selectedConnectors = Connectors.getSelectedConnectors(kafkaConnectApi, names, regexpMode);
        for (String connectorToPause : selectedConnectors) {
            kafkaConnectApi.pauseConnector(connectorToPause);
            spec.commandLine().getOut().println("Paused connector " + connectorToPause);
        }
    }
}
