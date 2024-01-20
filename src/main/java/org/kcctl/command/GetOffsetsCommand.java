/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import java.util.Set;
import java.util.concurrent.Callable;

import jakarta.inject.Inject;

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
