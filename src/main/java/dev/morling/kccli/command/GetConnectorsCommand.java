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

import java.util.List;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import dev.morling.kccli.service.ConnectorStatusInfo;
import dev.morling.kccli.service.KafkaConnectApi;
import dev.morling.kccli.service.TaskState;
import dev.morling.kccli.util.ConfigurationContext;
import picocli.CommandLine.Command;

import static dev.morling.kccli.util.Colors.ANSI_GREEN;
import static dev.morling.kccli.util.Colors.ANSI_RED;
import static dev.morling.kccli.util.Colors.ANSI_RESET;

@Command(name = "connectors", description = "Displays information about deployed connectors")
public class GetConnectorsCommand implements Runnable {

    @Inject
    ConfigurationContext context;

    @Override
    public void run() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getContext().getCluster())
                .build(KafkaConnectApi.class);

        List<String> connectors = kafkaConnectApi.getConnectors();
        String[][] data = new String[connectors.size()][];

        int i = 0;
        for (String name : connectors) {
            ConnectorStatusInfo connectorStatus = kafkaConnectApi.getConnectorStatus(name);
            data[i] = new String[]{
                    name,
                    " " + connectorStatus.type,
                    " " + connectorStatus.connector.state,
                    " " + toString(connectorStatus.tasks) };
            i++;
        }

        System.out.println();
        String table = AsciiTable.getTable(AsciiTable.NO_BORDERS,
                new Column[]{
                        new Column().header("NAME").dataAlign(HorizontalAlign.LEFT),
                        new Column().header(" TYPE").dataAlign(HorizontalAlign.LEFT),
                        new Column().header(" STATE").dataAlign(HorizontalAlign.LEFT),
                        new Column().header(" TASKS").dataAlign(HorizontalAlign.LEFT)
                },
                data);

        System.out.println(table.replace("RUNNING", ANSI_GREEN + "RUNNING" + ANSI_RESET).replace("FAILED", ANSI_RED + "FAILED" + ANSI_RESET));
        System.out.println();
    }

    private String toString(List<TaskState> tasks) {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for (TaskState taskState : tasks) {
            if (first) {
                first = false;
            }
            else {
                sb.append(", ");
            }

            sb.append(taskState.id + ": " + taskState.state);
        }

        return sb.toString();
    }
}
