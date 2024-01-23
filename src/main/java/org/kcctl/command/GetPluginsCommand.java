/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;

import jakarta.inject.Inject;

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

    @CommandLine.Mixin
    HelpMixin help;

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
        spec.commandLine().getOut().println(AsciiTable.getTable(AsciiTable.NO_BORDERS, connectorPlugins, List.of(
                new Column().header("TYPE").dataAlign(HorizontalAlign.LEFT).with(ConnectorPlugin::type),
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
