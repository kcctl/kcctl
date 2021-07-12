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
package dev.morling.kccli.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import dev.morling.kccli.service.ConnectorInfo;
import dev.morling.kccli.service.ConnectorStatusInfo;
import dev.morling.kccli.service.KafkaConnectApi;
import dev.morling.kccli.service.TaskState;
import dev.morling.kccli.service.TopicsInfo;
import dev.morling.kccli.util.ConfigurationContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import static dev.morling.kccli.util.Colors.ANSI_GREEN;
import static dev.morling.kccli.util.Colors.ANSI_RED;
import static dev.morling.kccli.util.Colors.ANSI_RESET;
import static dev.morling.kccli.util.Colors.ANSI_WHITE_BOLD;

@Command(name = "connector", description = "Displays information about a given connector")
public class DescribeConnectorCommand implements Runnable {

    @Inject
    ConfigurationContext context;

    @Parameters(paramLabel = "CONNECTOR NAME", description = "Name of the connector") // , completionCandidates = DummyCompletions.class)
    String name;

    @Override
    public void run() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCluster())
                .build(KafkaConnectApi.class);

        ConnectorInfo connector = kafkaConnectApi.getConnector(name);
        ConnectorStatusInfo connectorStatus = kafkaConnectApi.getConnectorStatus(name);
        Map<String, String> connectorConfig = kafkaConnectApi.getConnectorConfig(name);
        Map<String, TopicsInfo> connectorTopics = kafkaConnectApi.getConnectorTopics(name);

        List<Tuple> connectorInfo = Arrays.asList(
                new Tuple("Name", connector.name),
                new Tuple("Type", connectorStatus.type),
                new Tuple("State", colorizeState(connectorStatus.connector.state)),
                new Tuple("Worker ID", connectorStatus.connector.worker_id),
                new Tuple(ANSI_WHITE_BOLD + "Tasks" + ANSI_RESET, ""));

        printTuples(connectorInfo);

        for (TaskState task : connectorStatus.tasks) {
            printTuples(Arrays.asList(new Tuple("  " + task.id, "")));
            printTuples(Arrays.asList(
                    new Tuple("    State", colorizeState(task.state)),
                    new Tuple("    Worker ID", task.worker_id)));
        }

        List<Tuple> config = new ArrayList<>();

        for (Entry<String, String> configEntry : connectorConfig.entrySet()) {
            config.add(new Tuple("  " + configEntry.getKey(), configEntry.getValue()));
        }

        printTuples(Arrays.asList(new Tuple(ANSI_WHITE_BOLD + "Config" + ANSI_RESET, "")));
        printTuples(config);

        printTuples(Arrays.asList(new Tuple(ANSI_WHITE_BOLD + "Topics" + ANSI_RESET, "")));

        List<Tuple> topics = new ArrayList<>();

        for (String topic : connectorTopics.entrySet().iterator().next().getValue().topics) {
            topics.add(new Tuple("", "  " + topic));
        }
        printTuples(topics);
    }

    private String colorizeState(String state) {
        if (state.equals("RUNNING")) {
            return ANSI_GREEN + "RUNNING" + ANSI_RESET;
        }
        else if (state.equals("FAILED")) {
            return ANSI_RED + "FAILED" + ANSI_RESET;
        }
        else {
            return state;
        }
    }

    private void printTuples(List<Tuple> tuples) {
        if (tuples.isEmpty()) {
            return;
        }

        int maxLength = tuples.stream()
                .mapToInt(t -> t.key.length() + 1)
                .max()
                .getAsInt();

        for (Tuple tuple : tuples) {
            if (!tuple.key.isEmpty()) {
                System.out.printf("%-" + maxLength + "s  %s%n", tuple.key + ":", tuple.value);
            }
            else {
                System.out.println(tuple.value);
            }
        }
    }

    private static class Tuple {
        public String key;
        public String value;

        public Tuple(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
