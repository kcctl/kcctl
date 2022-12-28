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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.service.ConnectorPlugin;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Version;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "plugins", description = "Displays information about available connector plug-ins")
public class GetPluginsCommand implements Callable<Integer> {

    private final Version requiredVersionForAllPlugins = new Version(3, 2);

    private final ConfigurationContext context;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = { "-t", "--types" }, description = "Valid values: ${COMPLETION-CANDIDATES}", split = ",")
    Set<PluginType> pluginTypes;

    @Inject
    public GetPluginsCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public GetPluginsCommand() {
        context = new ConfigurationContext();
    }

    @Override
    public Integer call() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        Version currentVersion = new Version(kafkaConnectApi.getWorkerInfo().version());

        if (pluginTypes != null && !pluginTypes.stream().allMatch(t -> t == PluginType.SINK || t == PluginType.SOURCE)) {
            if (!currentVersion.greaterOrEquals(requiredVersionForAllPlugins)) {
                spec.commandLine().getErr().println("Listing plugins other than source and sink requires at least Kafka Connect 3.2. Current version: " + currentVersion);
                return 1;
            }
        }

        List<ConnectorPlugin> connectorPlugins = kafkaConnectApi.getConnectorPlugins(false);
        if (pluginTypes != null && !pluginTypes.isEmpty()) {
            connectorPlugins.removeIf(p -> !pluginTypes.contains(PluginType.forName(p.type())));
        }
        connectorPlugins.sort(Comparator.comparing(ConnectorPlugin::type).thenComparing(ConnectorPlugin::clazz));

        spec.commandLine().getOut().println();
        spec.commandLine().getOut().println(AsciiTable.getTable(AsciiTable.NO_BORDERS, connectorPlugins, Arrays.asList(
                new Column().header("TYPE").dataAlign(HorizontalAlign.LEFT).with(plugin -> plugin.type()),
                new Column().header(" CLASS").dataAlign(HorizontalAlign.LEFT).with(plugin -> " " + plugin.clazz()),
                new Column().header(" VERSION").dataAlign(HorizontalAlign.LEFT).with(plugin -> " " + (plugin.version() == null ? "n/a" : plugin.version())))));
        spec.commandLine().getOut().println();
        return 0;
    }

    public enum PluginType {
        SOURCE("source"),
        SINK("sink"),
        TRANSFORMATION("transformation"),
        CONVERTER("converter"),
        HEADER_CONVERTER("header_converter"),
        PREDICATE("predicate");

        public final String name;

        PluginType(String name) {
            this.name = name;
        }

        public static PluginType forName(String name) {
            return PluginType.valueOf(name.toUpperCase(Locale.ROOT));
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
