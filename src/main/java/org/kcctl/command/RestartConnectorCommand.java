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

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.completion.ConnectorNameCompletions;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "connector", description = "Restarts the specified connector")
public class RestartConnectorCommand implements Runnable {

    @Parameters(paramLabel = "NAME", description = "Name of the connector (e.g. 'my-connector')", completionCandidates = ConnectorNameCompletions.class)
    String name;

    @CommandLine.Option(names = { "-wt", "--with-tasks" }, description = "Also restart tasks; either ALL or FAILED")
    WithTasks withTasks;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    private final ConfigurationContext context;

    @Inject
    public RestartConnectorCommand(ConfigurationContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        String withTasksMessage = "";
        String includeTasks = "false";
        String onlyFailed = "false";
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        if (withTasks != null) {
            withTasksMessage = String.format(" with %s tasks", withTasks);
            includeTasks = "true";
            onlyFailed = withTasks.equals(WithTasks.FAILED) ? "true" : "false";
        }

        try {
            kafkaConnectApi.restartConnector(name, includeTasks, onlyFailed);
            spec.commandLine().getOut().println("Restarted connector " + name + withTasksMessage);
        }
        catch (Exception exception) {
            spec.commandLine().getOut().println(exception.getMessage());
        }

    }

    public enum WithTasks {
        ALL,
        FAILED
    }
}
