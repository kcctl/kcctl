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
import java.util.regex.Pattern;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.completion.PluginNameCompletions;
import org.kcctl.service.ConfigInfos;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Search;
import org.kcctl.util.Tuple;
import org.kcctl.util.Version;

import jakarta.inject.Inject;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import static org.kcctl.util.Colors.ANSI_RESET;
import static org.kcctl.util.Colors.ANSI_WHITE_BOLD;

@Command(name = "plugin", description = "Displays information about given plugin")
public class DescribePluginCommand implements Callable<Integer> {

    static class ConfigSearch {
        @CommandLine.Option(names = "--search", description = "Filter results to only properties whose name or docstring matches a given regex, using Java pattern syntax. Prefix with (?i) for case-insensitive searches")
        Pattern search;
        @CommandLine.Option(names = "--search-name", description = "Filter results to only properties whose name matches a given regex, using Java pattern syntax. Prefix with (?i) for case-insensitive searches")
        Pattern searchName;
        @CommandLine.Option(names = "--search-description", description = "Filter results to only properties whose docstring matches a given regex, using Java pattern syntax. Prefix with (?i) for case-insensitive searches")
        Pattern searchDescription;

        public List<ConfigInfos.ConfigKeyInfo> filterResults(List<ConfigInfos.ConfigKeyInfo> configs) {
            if (search != null) {
                return Search.searchConfig(configs, search);
            }
            else if (searchName != null) {
                return Search.searchConfigByName(configs, searchName);
            }
            else if (searchDescription != null) {
                return Search.searchConfigByDescription(configs, searchDescription);
            }
            else {
                // The if/else if branches here should be exhaustive; if for some reason picocli populates
                // an instance of this class but none of its members, we degrade gracefully here by not
                // performing any filtering of config results
                return configs;
            }
        }
    }

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.ArgGroup(exclusive = true)
    ConfigSearch configSearch;

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
        System.out.println();
        if (configSearch != null) {
            configs = configSearch.filterResults(configs);
        }
        for (ConfigInfos.ConfigKeyInfo config : configs) {
            Tuple.print(Arrays.asList(
                    new Tuple(ANSI_WHITE_BOLD + "Name" + ANSI_RESET, config.name()),
                    new Tuple(ANSI_WHITE_BOLD + "Type" + ANSI_RESET, config.type()),
                    new Tuple(ANSI_WHITE_BOLD + "Required" + ANSI_RESET, String.valueOf(config.required())),
                    new Tuple(ANSI_WHITE_BOLD + "Default" + ANSI_RESET, config.defaultValue()),
                    new Tuple(ANSI_WHITE_BOLD + "Documentation" + ANSI_RESET, config.documentation())));
            System.out.println();
        }

        return 0;
    }
}
