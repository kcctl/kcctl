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
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.completion.PluginNameCompletions;
import org.kcctl.service.ConfigInfos;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Version;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "plugin", description = "Displays information about given plugin")
public class DescribePluginCommand implements Callable<Integer> {

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Parameters(paramLabel = "PLUGIN NAME", description = "Name of the plugin", completionCandidates = PluginNameCompletions.class)
    String name;

    private final ConfigurationContext context;

    @Inject
    public DescribePluginCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public DescribePluginCommand() {
        context = new ConfigurationContext();
    }

    private final Version requiredVersion = new Version(3, 2);

    @Override
    public Integer call() {

        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);
        Version currentVersion = new Version(kafkaConnectApi.getWorkerInfo().version());

        if (!currentVersion.greaterOrEquals(requiredVersion)) {
            System.out.println("This command requires at least Kafka Connect 3.2. Current version: " + currentVersion);
            return 1;
        }

        List<ConfigInfos.ConfigKeyInfo> configs = kafkaConnectApi.getConnectorPluginConfig(name);
        spec.commandLine().getOut().println();
        spec.commandLine().getOut().println(AsciiTable.getTable(AsciiTable.NO_BORDERS, configs, Arrays.asList(
                new Column().header("NAME").dataAlign(HorizontalAlign.LEFT).with(config -> config.name()),
                new Column().header(" TYPE").dataAlign(HorizontalAlign.LEFT).with(config -> " " + config.type()),
                new Column().header(" REQUIRED").dataAlign(HorizontalAlign.LEFT).with(config -> " " + config.required()),
                new Column().header(" DEFAULT").dataAlign(HorizontalAlign.LEFT).with(config -> " " + config.defaultValue()),
                new Column().header(" DOCUMENTATION").dataAlign(HorizontalAlign.LEFT).with(config -> " " + config.documentation()))));
        spec.commandLine().getOut().println();

        return 0;
    }
}
