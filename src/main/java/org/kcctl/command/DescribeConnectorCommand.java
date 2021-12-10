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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.completion.ConnectorNameCompletions;
import org.kcctl.service.ConnectorInfo;
import org.kcctl.service.ConnectorStatusInfo;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.service.TaskState;
import org.kcctl.service.TopicsInfo;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Tuple;
import org.kcctl.util.Version;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import static org.kcctl.util.Colors.ANSI_GREEN;
import static org.kcctl.util.Colors.ANSI_RED;
import static org.kcctl.util.Colors.ANSI_RESET;
import static org.kcctl.util.Colors.ANSI_WHITE_BOLD;
import static org.kcctl.util.Colors.ANSI_YELLOW;

@Command(name = "connectorName", description = "Displays information about a given connectorName")
public class DescribeConnectorCommand implements Callable<Integer> {

    @Inject
    ConfigurationContext context;

    @Parameters(paramLabel = "CONNECTOR NAME", description = "Name of the connectorName", completionCandidates = ConnectorNameCompletions.class)
    String name;

    @Option(names = { "--tasks-config" }, description = "Displays tasks configuration")
    boolean includeTasksConfig;

    private final Version requiredVersionForTasksConfig = new Version(2, 8);
    private final Version requiredVersionForTopicsApi = new Version(2, 5);

    @Override
    public Integer call() {

        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);
        Version currentVersion = new Version(kafkaConnectApi.getWorkerInfo().version);

        if (includeTasksConfig) {
            if (!currentVersion.greaterOrEquals(requiredVersionForTasksConfig)) {
                System.out.println("--tasks-config requires at least Kafka Connect 2.8. Current version: " + currentVersion);
                return 1;
            }
        }

        try {
            ConnectorInfo connector = kafkaConnectApi.getConnector(name);
            ConnectorStatusInfo connectorStatus = kafkaConnectApi.getConnectorStatus(name);
            Map<String, String> connectorConfig = kafkaConnectApi.getConnectorConfig(name);

            List<Tuple> connectorInfo = Arrays.asList(
                    new Tuple("Name", connector.name),
                    new Tuple("Type", connectorStatus.type),
                    new Tuple("State", colorizeState(connectorStatus.connector.state)),
                    new Tuple("Worker ID", connectorStatus.connector.worker_id));

            Tuple.print(connectorInfo);

            // Config
            List<Tuple> config = new ArrayList<>();

            for (Entry<String, String> configEntry : connectorConfig.entrySet()) {
                config.add(new Tuple("  " + configEntry.getKey(), configEntry.getValue()));
            }

            Tuple.print(Arrays.asList(new Tuple(ANSI_WHITE_BOLD + "Config" + ANSI_RESET, "")));
            Tuple.print(config);

            Tuple.print(Arrays.asList(new Tuple(ANSI_WHITE_BOLD + "Tasks" + ANSI_RESET, "")));

            Map<String, Map<String, String>> tasksConfigs;
            if (includeTasksConfig) {
                tasksConfigs = kafkaConnectApi.getConnectorTasksConfig(name);
            }
            else {
                tasksConfigs = Collections.emptyMap();
            }

            // Tasks
            for (TaskState task : connectorStatus.tasks) {
                Tuple.print(Arrays.asList(new Tuple("  " + task.id, "")));
                List<Tuple> tuples = new ArrayList<>();
                tuples.add(new Tuple("    State", colorizeState(task.state)));
                tuples.add(new Tuple("    Worker ID", task.worker_id));

                if (includeTasksConfig) {
                    tuples.add(new Tuple("    Config", ""));

                    for (Entry<String, String> taskConfig : tasksConfigs.get(name + "-" + task.id).entrySet()) {
                        tuples.add(new Tuple("      " + taskConfig.getKey(), taskConfig.getValue()));
                    }
                }

                if (task.state.equals("FAILED")) {
                    tuples.add(new Tuple("    Trace", task.trace.replaceAll("Caused by", "      Caused by")));
                }
                Tuple.print(tuples);
            }

            if (currentVersion.greaterOrEquals(requiredVersionForTopicsApi)) {

                Map<String, TopicsInfo> connectorTopics = kafkaConnectApi.getConnectorTopics(name);

                Tuple.print(Arrays.asList(new Tuple(ANSI_WHITE_BOLD + "Topics" + ANSI_RESET, "")));

                List<Tuple> topics = new ArrayList<>();

                for (String topic : connectorTopics.entrySet().iterator().next().getValue().topics) {
                    topics.add(new Tuple("", "  " + topic));
                }
                Tuple.print(topics);
            }
        }
        catch (Exception e) {
            if (!e.getMessage().contains("not found")) {
                throw e;
            }

            System.out.println("Connector " + name + " not found. The following connectorName(s) are available:");

            GetConnectorsCommand getConnectors = new GetConnectorsCommand();
            getConnectors.context = context;
            getConnectors.run();

            return 1;
        }

        return 0;
    }

    private String colorizeState(String state) {
        if (state.equals("RUNNING")) {
            return ANSI_GREEN + "RUNNING" + ANSI_RESET;
        }
        else if (state.equals("PAUSED")) {
            return ANSI_YELLOW + "PAUSED" + ANSI_RESET;
        }
        else if (state.equals("FAILED")) {
            return ANSI_RED + "FAILED" + ANSI_RESET;
        }
        else if (state.equals("UNASSIGNED")) {
            return ANSI_YELLOW + "UNASSIGNED" + ANSI_RESET;
        }
        else {
            return state;
        }
    }
}
