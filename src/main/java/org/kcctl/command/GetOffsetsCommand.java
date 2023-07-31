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

import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.completion.ConnectorNameCompletions;
import org.kcctl.service.ConnectorOffsets;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Version;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine;

/**
 * Retrieve the committed offsets for a connector.
 *
 * @see <a href="https://cwiki.apache.org/confluence/display/KAFKA/KIP-875%3A+First-class+offsets+support+in+Kafka+Connect">KIP-875</a>
 */
@CommandLine.Command(name = "offsets", description = "Displays information about committed connector offsets")
public class GetOffsetsCommand implements Callable<Integer> {

    @CommandLine.Mixin
    HelpMixin help;

    private final Version requiredVersionForReadingOffsets = new Version(3, 5);
    private final ObjectMapper mapper = new ObjectMapper();
    private final ConfigurationContext context;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Parameters(paramLabel = "NAME", description = "Name of the connector (e.g. 'my-connector')", completionCandidates = ConnectorNameCompletions.class)
    Set<String> names = Set.of();

    @Inject
    public GetOffsetsCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public GetOffsetsCommand() {
        context = new ConfigurationContext();
    }

    @Override
    public Integer call() throws JsonProcessingException {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        Version currentVersion = new Version(kafkaConnectApi.getWorkerInfo().version());

        if (!currentVersion.greaterOrEquals(requiredVersionForReadingOffsets)) {
            spec.commandLine().getErr().println("Reading connector offsets requires at least Kafka Connect 3.5. Current version: " + currentVersion);
            return 1;
        }

        for (String connector : names) {
            ConnectorOffsets offsets = kafkaConnectApi.getConnectorOffsets(connector);
            // Display with JSON; the contents aren't very readable as raw text, and this output format
            // plays nicely with the endpoint to patch connector offsets (you can use the output here as the
            // body for requests to that endpoint)
            String offsetsJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(offsets);
            spec.commandLine().getOut().println(offsetsJson);
        }

        return 0;
    }

}
