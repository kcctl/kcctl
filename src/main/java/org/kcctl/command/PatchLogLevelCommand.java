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
import java.util.List;
import java.util.concurrent.Callable;

import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.completion.LoggerNameCompletions;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Version;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import picocli.CommandLine;

@CommandLine.Command(name = "logger", description = "Changes the log level of given class/Connector path")
public class PatchLogLevelCommand implements Callable<Object> {

    @Inject
    public PatchLogLevelCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public PatchLogLevelCommand() {
        context = new ConfigurationContext();
    }

    @CommandLine.Mixin
    HelpMixin help;

    private final Version requiredVersionForClusterScope = new Version(3, 7);

    @Inject
    ConfigurationContext context;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Parameters(paramLabel = "Logger NAME", description = "Name of the logger", completionCandidates = LoggerNameCompletions.class)
    String name;

    @CommandLine.Option(names = { "-l", "--level" }, description = "Name of log level to apply", required = true)
    LogLevel level;

    @CommandLine.Option(names = { "-s",
            "--scope" }, description = "The scope of the logging adjustment; e.g., 'worker' or 'cluster'", completionCandidates = ScopeCompletions.class)
    String scope;

    static class ScopeCompletions implements Iterable<String> {
        private static final List<String> SCOPES = List.of("worker", "cluster");

        @Override
        public Iterator<String> iterator() {
            return SCOPES.iterator();
        }
    }

    @Override
    public Object call() throws Exception {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode data = mapper.createObjectNode();
        data.put("level", level.name());
        if (scope == null) {
            List<String> classes = kafkaConnectApi.updateLogLevel(name, mapper.writeValueAsString(data));
            for (String s : classes) {
                spec.commandLine().getOut().println(s);
            }
        }
        else if ("worker".equals(scope)) {
            String response = kafkaConnectApi.updateLogLevelWithScope(name, scope, mapper.writeValueAsString(data));
            List<String> classes = mapper.readValue(response, new TypeReference<>() {
            });
            for (String s : classes) {
                spec.commandLine().getOut().println(s);
            }
        }
        else if ("cluster".equals(scope)) {
            Version currentVersion = new Version(kafkaConnectApi.getWorkerInfo().version());

            if (!currentVersion.greaterOrEquals(requiredVersionForClusterScope)) {
                spec.commandLine().getErr().printf("Cluster-wide logging adjustments requires at least Kafka Connect %s. Current version: %s",
                        requiredVersionForClusterScope, currentVersion);
                return 1;
            }

            kafkaConnectApi.updateLogLevelWithScope(name, scope, mapper.writeValueAsString(data));
            spec.commandLine().getOut().println("Updated log level");
        }
        else {
            String response = kafkaConnectApi.updateLogLevelWithScope(name, scope, mapper.writeValueAsString(data));
            spec.commandLine().getOut().println(response);
        }

        return 0;
    }

    public static enum LogLevel {
        ERROR,
        WARN,
        FATAL,
        DEBUG,
        INFO,
        TRACE;
    }
}
