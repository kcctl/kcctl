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

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.completion.ConnectorNameCompletions;
import org.kcctl.service.ConnectorStatusInfo;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Connectors;
import org.kcctl.util.Version;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "connector", aliases = "connectors", description = "Restarts the specified connector(s)")
public class RestartConnectorCommand implements Callable<Integer> {

    private final Version requiredVersionForIncludingTasks = new Version(3, 0);
    @CommandLine.Option(names = { "-e", "--reg-exp" }, description = "use NAME(s) as regexp pattern(s) to use on all connectors")
    boolean regexpMode = false;

    @CommandLine.Option(names = { "-t", "--tasks" }, description = "also restart tasks for the connector(s); valid values: ${COMPLETION-CANDIDATES}")
    Tasks tasks = null;

    @Parameters(paramLabel = "NAME", description = "Name of the connector (e.g. 'my-connector')", completionCandidates = ConnectorNameCompletions.class)
    Set<String> names = Set.of();

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    private final ConfigurationContext context;

    @Inject
    public RestartConnectorCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public RestartConnectorCommand() {
        context = new ConfigurationContext();
    }

    @Override
    public Integer call() {
        boolean includeTasks = tasks != null;
        boolean onlyFailed = includeTasks && tasks == Tasks.FAILED;

        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        Version currentVersion = new Version(kafkaConnectApi.getWorkerInfo().version());

        if (includeTasks && !currentVersion.greaterOrEquals(requiredVersionForIncludingTasks)) {
            spec.commandLine().getErr().println("Restarting tasks en masse requires at least Kafka Connect 3.0. Current version: " + currentVersion);
            spec.commandLine().getErr().println("Individual tasks may be still be restarted using the 'restart task' command");
            return CommandLine.ExitCode.SOFTWARE;
        }

        Set<String> selectedConnectors = Connectors.getSelectedConnectors(kafkaConnectApi, names, regexpMode);
        for (String connectorToRestart : selectedConnectors) {
            if (includeTasks) {
                ConnectorStatusInfo statusInfo = kafkaConnectApi.restartConnectorAndTasks(connectorToRestart, true, onlyFailed);
                long numRestartedTasks = statusInfo.tasks().stream()
                        .filter(t -> "RESTARTING".equals(t.state()))
                        .count();
                spec.commandLine().getOut().println("Restarted connector " + connectorToRestart + " and " + numRestartedTasks + " task(s)");
            }
            else {
                kafkaConnectApi.restartConnector(connectorToRestart);
                spec.commandLine().getOut().println("Restarted connector " + connectorToRestart);
            }
        }

        return CommandLine.ExitCode.OK;
    }

    public enum Tasks {
        ALL("all"),
        FAILED("failed");

        public final String name;

        Tasks(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
