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
package dev.morling.kccli.command;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.morling.kccli.completion.ConnectorNameCompletions;
import dev.morling.kccli.service.KafkaConnectApi;
import dev.morling.kccli.service.KafkaConnectException;
import dev.morling.kccli.util.ConfigurationContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "connector", description = "Patches the specified connector with the given configuration parameters")
public class PatchConnectorCommand implements Callable<Integer> {

    @Spec
    CommandSpec commandSpec;

    @Inject
    ConfigurationContext context;

    @Parameters(paramLabel = "CONNECTOR NAME", description = "Name of the connector", completionCandidates = ConnectorNameCompletions.class)
    String name;

    @Option(names = { "-s", "--set" }, description = "Set the following configuration parameter")
    Map<String, String> setParameters;

    @Option(names = { "-r", "--remove" }, description = "Remove the following configuration parameter")
    List<String> removeParameters;

    @Override
    public Integer call() throws JsonProcessingException {

        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        Map<String, String> connectorParameters = kafkaConnectApi.getConnectorConfig(name);

        if (setParameters == null && removeParameters == null) {

            throw new ParameterException(commandSpec.commandLine(), "Missing required arguments: " +
                    " please enter at least one parameter to set or remove");
        }

        if (setParameters != null) {
            connectorParameters.putAll(setParameters);
        }

        if (removeParameters != null) {
            removeParameters.forEach(connectorParameters::remove);
        }

        String connectorParametersString = new ObjectMapper().writeValueAsString(connectorParameters);
        kafkaConnectApi.updateConnector(name, connectorParametersString);

        DescribeConnectorCommand describeConnectorCommand = new DescribeConnectorCommand();
        describeConnectorCommand.context = context;
        describeConnectorCommand.name = name;
        describeConnectorCommand.includeTasksConfig = false;

        System.out.println("New connector configuration:");

        Instant start = Instant.now();

        while (Duration.between(start, Instant.now()).toSeconds() < 30) {
            try {
                return describeConnectorCommand.call();
            }
            catch (KafkaConnectException kce) {
                if (kce.getMessage().startsWith("Cannot complete request momentarily due to stale configuration")) {
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException ie) {
                        throw new RuntimeException(ie);
                    }
                }
                else {
                    throw kce;
                }
            }
        }

        return 0;
    }
}
