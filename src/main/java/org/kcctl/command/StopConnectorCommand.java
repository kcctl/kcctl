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

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.completion.ConnectorNameCompletions;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Connectors;
import org.kcctl.util.Version;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "connector", aliases = "connectors", description = "Stops (but does not delete) the specified connector(s)")
public class StopConnectorCommand implements Callable<Integer> {

    @CommandLine.Mixin
    HelpMixin help;

    private final Version requiredVersionForStoppingConnectors = new Version(3, 5);

    @CommandLine.Option(names = { "-e", "--reg-exp" }, description = "use NAME(s) as regexp pattern(s) to use on all connectors")
    boolean regexpMode = false;

    @Parameters(paramLabel = "NAME", description = "Name(s) of the connector(s) (e.g. 'my-connector').", completionCandidates = ConnectorNameCompletions.class)
    Set<String> names = Set.of();

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    private final ConfigurationContext context;

    @Inject
    public StopConnectorCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public StopConnectorCommand() {
        context = new ConfigurationContext();
    }

    @Override
    public Integer call() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        Version currentVersion = new Version(kafkaConnectApi.getWorkerInfo().version());

        if (!currentVersion.greaterOrEquals(requiredVersionForStoppingConnectors)) {
            spec.commandLine().getErr().println("Stopping connectors requires at least Kafka Connect 3.5. Current version: " + currentVersion);
            return 1;
        }

        Set<String> selectedConnectors = Connectors.getSelectedConnectors(kafkaConnectApi, names, regexpMode);

        for (String connectorToStop : selectedConnectors) {
            kafkaConnectApi.stopConnector(connectorToStop);
            spec.commandLine().getOut().println("Stopped connector " + connectorToStop);
        }

        return 0;
    }
}
