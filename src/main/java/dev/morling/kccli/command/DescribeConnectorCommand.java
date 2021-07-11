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
import dev.morling.kccli.util.ConfigurationContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "connector", description = "Displays information about a given connector")
public class DescribeConnectorCommand implements Runnable {

    @Inject
    ConfigurationContext context;

    @Parameters(paramLabel = "CONNECTOR NAME", description = "Name of the connector")
    String name;

    @Override
    public void run() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCluster())
                .build(KafkaConnectApi.class);

        ConnectorInfo connector = kafkaConnectApi.getConnector(name);
        ConnectorStatusInfo connectorStatus = kafkaConnectApi.getConnectorStatus(name);
        Map<String, String> connectorConfig = kafkaConnectApi.getConnectorConfig(name);

        List<Tuple> connectorInfo = Arrays.asList(
                new Tuple("Name", connector.name),
                new Tuple("Type", connectorStatus.type),
                new Tuple("State", connectorStatus.connector.state),
                new Tuple("Worker ID", connectorStatus.connector.worker_id),
                new Tuple("Tasks", ""));

        printTuples(connectorInfo);

        for (TaskState task : connectorStatus.tasks) {
            printTuples(Arrays.asList(new Tuple("  " + task.id, "")));
            printTuples(Arrays.asList(
                    new Tuple("    State", task.state),
                    new Tuple("    Worker ID", task.worker_id)));
        }

        List<Tuple> config = new ArrayList<>();

        for (Entry<String, String> configEntry : connectorConfig.entrySet()) {
            config.add(new Tuple("  " + configEntry.getKey(), configEntry.getValue()));
        }

        printTuples(Arrays.asList(new Tuple("Config", "")));
        printTuples(config);

        // String[] headers = { "Name: ", connector.name };
        // String[][] data = new String[][] {
        // { "State: ", connectorStatus.connector.state },
        // { "Worker ID: ", connectorStatus.connector.worker_id },
        // { "Type: ", connectorStatus.type },
        // { "Config: ", "" },
        // { " " + connectorConfig.entrySet().iterator().next().getKey() + ":", connectorConfig.entrySet().iterator().next().getValue() }
        // };
        //
        // System.out.println(AsciiTable.getTable(headers, data));
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
            System.out.printf("%-" + maxLength + "s  %s%n", tuple.key + ":", tuple.value);
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
