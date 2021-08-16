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

import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.morling.kccli.service.KafkaConnectApi;
import dev.morling.kccli.util.ConfigurationContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "connector", description = "Patches the specified connector with the given configuration parameters")
public class PatchConnectorCommand implements Callable<Integer> {

    @Inject
    ConfigurationContext context;

    @Parameters(paramLabel = "CONNECTOR NAME", description = "Name of the connector")
    String name;

    @Option(names = { "-p", "--parameter" }, description = "Configuration parameters for the connector", required = true)
    Map<String, String> patchParameters;

    @Override
    public Integer call() throws JsonProcessingException {

        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCluster())
                .build(KafkaConnectApi.class);

        Map<String, String> connectorParameters = kafkaConnectApi.getConnectorConfig(name);
        connectorParameters.putAll(patchParameters);

        String connectorParametersString = new ObjectMapper().writeValueAsString(connectorParameters);
        kafkaConnectApi.updateConnector(name, connectorParametersString);

        DescribeConnectorCommand describeConnectorCommand = new DescribeConnectorCommand();
        describeConnectorCommand.context = context;
        describeConnectorCommand.name = name;
        describeConnectorCommand.includeTasksConfig = false;

        return describeConnectorCommand.call();
    }
}
