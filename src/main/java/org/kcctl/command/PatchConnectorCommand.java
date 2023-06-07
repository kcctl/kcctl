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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.completion.ConnectorNameCompletions;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.service.KafkaConnectException;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Connectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "connector", aliases = "connectors", description = "Patches the specified connector(s) with the given configuration parameters")
public class PatchConnectorCommand implements Callable<Integer> {

    @Spec
    CommandSpec commandSpec;

    @Parameters(paramLabel = "CONNECTOR NAME", description = "Name of the connector", completionCandidates = ConnectorNameCompletions.class)
    Set<String> names = Set.of();

    @Option(names = { "-e", "--reg-exp" }, description = "use CONNECTOR NAME(s) as regexp pattern(s) to use on all connectors")
    boolean regexpMode = false;

    @Option(names = { "-s", "--set" }, description = "Set the following configuration parameter")
    Map<String, String> setParameters;

    @Option(names = { "-r", "--remove" }, description = "Remove the following configuration parameter")
    List<String> removeParameters;

    private final ConfigurationContext context;

    @Inject
    public PatchConnectorCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public PatchConnectorCommand() {
        context = new ConfigurationContext();
    }

    @Override
    public Integer call() throws JsonProcessingException, InterruptedException, ExecutionException {

        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        Set<String> selectedConnector = Connectors.getSelectedConnectors(kafkaConnectApi, names, regexpMode);
        for (String connectorToPatch : selectedConnector) {
            int returnCode = patch(kafkaConnectApi, connectorToPatch);
            if (returnCode > 0)
                return returnCode;
        }

        return 0;
    }

    private int patch(KafkaConnectApi kafkaConnectApi, String conectorToPatch) throws JsonProcessingException {
        Map<String, String> connectorParameters = kafkaConnectApi.getConnectorConfig(conectorToPatch);

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
        kafkaConnectApi.updateConnector(conectorToPatch, connectorParametersString);

        DescribeConnectorCommand describeConnectorCommand = new DescribeConnectorCommand(context);
        describeConnectorCommand.names = Set.of(conectorToPatch);
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
