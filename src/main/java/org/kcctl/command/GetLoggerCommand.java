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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.completion.LoggerNameCompletions;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    @Parameters(paramLabel = "LOGGER NAME", description = "Name of the logger", completionCandidates = LoggerNameCompletions.class)
    String path;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    private static final String DEFAULT_PATH = "ALL";

    private final ConfigurationContext context;

    public GetLoggerCommand(ConfigurationContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        String[][] data;
        if (path.equals(DEFAULT_PATH)) {
            // all
            ObjectNode connectorLoggers = kafkaConnectApi.getLoggers("");
            Iterator<String> classPaths = connectorLoggers.fieldNames();

            data = new String[connectorLoggers.size()][];

            int i = 0;
            for (final JsonNode header : (Iterable<JsonNode>) connectorLoggers::elements) {
                for (final Map.Entry<String, JsonNode> field : (Iterable<Map.Entry<String, JsonNode>>) header::fields) {
                    data[i] = new String[]{
                            classPaths.next(),
                            " " + field.getValue().textValue()
                    };
                }
                i++;
            }
        }
        else {
            ObjectNode connectorLoggers = kafkaConnectApi.getLoggers(path);
            data = new String[connectorLoggers.size()][];
            data[0] = new String[]{
                    path,
                    connectorLoggers.findValue("level").textValue()
            };
        }
        spec.commandLine().getOut().println();
        String table = AsciiTable.getTable(AsciiTable.NO_BORDERS,
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
}
