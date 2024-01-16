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
