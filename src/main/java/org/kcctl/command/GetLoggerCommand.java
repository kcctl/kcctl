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

import java.util.Map;
import java.util.function.Function;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.completion.LoggerNameCompletions;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.service.LoggerLevel;
import org.kcctl.util.ConfigurationContext;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import picocli.CommandLine;
import picocli.CommandLine.Parameters;

import static org.kcctl.util.Colors.ANSI_CYAN;
import static org.kcctl.util.Colors.ANSI_GREEN;
import static org.kcctl.util.Colors.ANSI_RED;
import static org.kcctl.util.Colors.ANSI_RESET;
import static org.kcctl.util.Colors.ANSI_YELLOW;

@CommandLine.Command(name = "logger", description = "Displays information about a specific logger")
public class GetLoggerCommand implements Runnable {

    @CommandLine.Mixin
    HelpMixin help;

    @Parameters(paramLabel = "LOGGER NAME", description = "Name of the logger", completionCandidates = LoggerNameCompletions.class)
    String path;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    private static final String DEFAULT_PATH = "ALL";

    private final ConfigurationContext context;

    public GetLoggerCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public GetLoggerCommand() {
        context = new ConfigurationContext();
    }

    @Override
    public void run() {
        String[][] data;
        if (path.equals(DEFAULT_PATH)) {
            // all
            // TODO: This duplicates logic in the 'get loggers' command
            Map<String, LoggerLevel> loggers = allLoggers();
            data = loggers.entrySet().stream()
                    .map(e -> {
                        String logger = e.getKey();
                        LoggerLevel loggerLevel = e.getValue();
                        String level = " " + loggerLevel.level();
                        return new String[]{ logger, level };
                    }).toArray(String[][]::new);
        }
        else {
            LoggerLevel loggerLevel = logger(path);
            String[] row = new String[]{
                    path,
                    loggerLevel.level()
            };
            data = new String[][]{ row };
        }
        spec.commandLine().getOut().println();
        String table = AsciiTable.getTable(AsciiTable.NO_BORDERS,
                // TODO: Add last_modified field to table
                new Column[]{
                        new Column().header("LOGGER").dataAlign(HorizontalAlign.LEFT),
                        new Column().header(" LEVEL").dataAlign(HorizontalAlign.LEFT)
                },
                data);
        spec.commandLine().getOut().println(table.replace("ERROR", ANSI_RED + "ERROR" + ANSI_RESET)
                .replace("WARN", ANSI_RED + "WARN" + ANSI_RESET)
                .replace("FATAL", ANSI_RED + "FATAL" + ANSI_RESET)
                .replace("DEBUG", ANSI_YELLOW + "DEBUG" + ANSI_RESET)
                .replace("INFO", ANSI_GREEN + "INFO" + ANSI_RESET)
                .replace("TRACE", ANSI_CYAN + "TRACE" + ANSI_RESET));
    }

    // visible for testing
    Map<String, LoggerLevel> allLoggers() {
        return kafkaConnectRequest(KafkaConnectApi::getLoggers);
    }

    // visible for testing
    LoggerLevel logger(String path) {
        return kafkaConnectRequest(kafkaConnectApi -> kafkaConnectApi.getLogger(path));
    }

    private <T> T kafkaConnectRequest(Function<KafkaConnectApi, T> request) {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);
        return request.apply(kafkaConnectApi);
    }
}
