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

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.completion.LoggerNameCompletions;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import picocli.CommandLine;

@CommandLine.Command(name = "logger", description = "Changes the log level of given class/Connector path")
public class PatchLogLevelCommand implements Callable<Object> {

    @CommandLine.Mixin
    HelpMixin help;

    @Inject
    ConfigurationContext context;

    @CommandLine.Parameters(paramLabel = "Logger NAME", description = "Name of the logger", completionCandidates = LoggerNameCompletions.class)
    String name;

    @CommandLine.Option(names = { "-l", "--level" }, description = "Name of log level to apply", required = true)
    LogLevel level;

    @Override
    public Object call() throws Exception {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode data = mapper.createObjectNode();
        data.put("level", level.name());
        List<String> classes = kafkaConnectApi.updateLogLevel(name, mapper.writeValueAsString(data));
        for (String s : classes) {
            System.out.println(s);
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
