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
import org.kcctl.service.AlterResetOffsetsResponse;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Version;

import picocli.CommandLine;

/**
 * Deletes the committed offsets for a connector.
 *
 * @see <a href="https://cwiki.apache.org/confluence/display/KAFKA/KIP-875%3A+First-class+offsets+support+in+Kafka+Connect">KIP-875</a>
 */
@CommandLine.Command(name = "offsets", description = "Delete committed connector offsets")
public class DeleteOffsetsCommand implements Callable<Integer> {

    @CommandLine.Mixin
    HelpMixin help;

    private final Version requiredVersionForDeletingOffsets = new Version(3, 6);
    private final ConfigurationContext context;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Parameters(paramLabel = "NAME", description = "Name(s) of the connector(s) (e.g. 'my-connector')", completionCandidates = ConnectorNameCompletions.class)
    Set<String> names = Set.of();

    @Inject
    public DeleteOffsetsCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public DeleteOffsetsCommand() {
        context = new ConfigurationContext();
    }

    @Override
    public Integer call() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        Version currentVersion = new Version(kafkaConnectApi.getWorkerInfo().version());

        if (!currentVersion.greaterOrEquals(requiredVersionForDeletingOffsets)) {
            spec.commandLine().getErr().println(String.format("Deleting connector offsets requires at least Kafka Connect %s. Current version: %s",
                    requiredVersionForDeletingOffsets, currentVersion));
            return 1;
        }

        for (String connector : names) {
            AlterResetOffsetsResponse offsets = kafkaConnectApi.deleteConnectorOffsets(connector);
            spec.commandLine().getOut().println(offsets.message());
        }

        return 0;
    }

}
