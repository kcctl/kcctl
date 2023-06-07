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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.service.ConnectorExpandInfo;
import org.kcctl.service.ConnectorStatusInfo;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.service.TaskState;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.GetConnectorsStatusStyler;
import org.kcctl.util.Version;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "connectors", description = "Displays information about deployed connectors")
public class GetConnectorsCommand implements Runnable {

    private final ConfigurationContext context;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Inject
    public GetConnectorsCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public GetConnectorsCommand() {
        context = new ConfigurationContext();
    }

    // Invoke by describe connector command when connector not found
    public GetConnectorsCommand(ConfigurationContext context, CommandLine.Model.CommandSpec spec) {
        this.context = context;
        this.spec = spec;
    }

    private final Version requiredVersionForExpandApi = new Version(2, 3);

    @Override
    public void run() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        Version currentVersion = new Version(kafkaConnectApi.getWorkerInfo().version());

        List<ConnectorStatusInfo> connectors;
        if (currentVersion.greaterOrEquals(requiredVersionForExpandApi)) {
            connectors = kafkaConnectApi.getConnectorExpandInfo(List.of("status")).values().stream()
                    .map(ConnectorExpandInfo::status)
                    .sorted(Comparator.comparing(ConnectorStatusInfo::type).thenComparing(ConnectorStatusInfo::name))
                    .collect(Collectors.toList());
        }
        else {
            connectors = kafkaConnectApi.getConnectors().stream()
                    .map(kafkaConnectApi::getConnectorStatus)
                    .collect(Collectors.toList());
        }

        String[][] data = new String[connectors.size()][];

        int i = 0;
        for (ConnectorStatusInfo status : connectors) {
            data[i] = new String[]{
                    status.name(),
                    " " + status.type(),
                    " " + status.connector().state(),
                    " " + toString(status.tasks()) };
            i++;
        }

        spec.commandLine().getOut().println();
        Column[] columns = new Column[]{
                new Column().header("NAME").dataAlign(HorizontalAlign.LEFT),
                new Column().header(" TYPE").dataAlign(HorizontalAlign.LEFT),
                new Column().header(" STATE").dataAlign(HorizontalAlign.LEFT),
                new Column().header(" TASKS").dataAlign(HorizontalAlign.LEFT).maxWidth(100)
        };
        String table = AsciiTable.builder()
                .data(columns, data)
                .border(AsciiTable.NO_BORDERS)
                .styler(new GetConnectorsStatusStyler())
                .asString();

        spec.commandLine().getOut().println(table);
        spec.commandLine().getOut().println();
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

            sb.append(taskState.id()).append(": ").append(taskState.state());
        }

        return sb.toString();
    }
}
