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

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.service.ConnectorPlugin;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "plugins", description = "Displays information about available connector plug-ins")
public class GetPluginsCommand implements Runnable {

    private final ConfigurationContext context;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Inject
    public GetPluginsCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public GetPluginsCommand() {
        context = new ConfigurationContext();
    }

    @Override
    public void run() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        List<ConnectorPlugin> connectorPlugins = kafkaConnectApi.getConnectorPlugins();
        connectorPlugins.sort((c1, c2) -> -c1.type().compareTo(c2.type()));

        spec.commandLine().getOut().println();
        spec.commandLine().getOut().println(AsciiTable.getTable(AsciiTable.NO_BORDERS, connectorPlugins, Arrays.asList(
                new Column().header("TYPE").dataAlign(HorizontalAlign.LEFT).with(plugin -> plugin.type()),
                new Column().header(" CLASS").dataAlign(HorizontalAlign.LEFT).with(plugin -> " " + plugin.clazz()),
                new Column().header(" VERSION").dataAlign(HorizontalAlign.LEFT).with(plugin -> " " + plugin.version()))));
        spec.commandLine().getOut().println();
    }
}
