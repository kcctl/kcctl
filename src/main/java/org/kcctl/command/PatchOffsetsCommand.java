/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.completion.ConnectorNameCompletions;
import org.kcctl.service.AlterResetOffsetsResponse;
import org.kcctl.service.ConnectorOffset;
import org.kcctl.service.ConnectorOffsets;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Version;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine;

/**
 * Patches the committed offsets for a connector.
 *
 * @see <a href="https://cwiki.apache.org/confluence/display/KAFKA/KIP-875%3A+First-class+offsets+support+in+Kafka+Connect">KIP-875</a>
 */
@CommandLine.Command(name = "offsets", description = "Patches committed connector offsets")
public class PatchOffsetsCommand implements Callable<Integer> {

    @CommandLine.Mixin
    HelpMixin help;

    private final Version requiredVersionForPatchingOffsets = new Version(3, 6);
    private final ConfigurationContext context;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Parameters(paramLabel = "NAME", description = "Name of the connector (e.g. 'my-connector')", completionCandidates = ConnectorNameCompletions.class)
    String name;

    static class SinkConnectorOffset {
        @CommandLine.Option(names = "--kafka-topic", required = true, description = "Name of the Kafka topic (for sink connectors)")
        String topic;

        @CommandLine.Option(names = "--kafka-partition", required = true, description = "Partition of the Kafka topic (for sink connectors)")
        int partition;

        @CommandLine.Option(names = "--kafka-offset", required = true, description = "Desired offset of the Kafka topic (for sink connectors)")
        long offset;
    }

    static class SourceConnectorOffset {
        @CommandLine.Option(names = "--source-partition", required = true, type = Map.class, converter = JsonObjectConverter.class, description = "Partition (for source connectors), as a JSON object")
        Object partition;

        @CommandLine.Option(names = "--source-offset", required = true, type = Map.class, converter = JsonObjectConverter.class, description = "Desired offset (for source connectors), as a JSON object")
        Object offset;

        @SuppressWarnings("unchecked")
        Map<String, Object> partition() {
            // Dirty hack to evade PicoCLI's automatic parsing for Map fields,
            // which it expects to come in key=value format
            // We declare the field with a type of Object, but a converter that
            // automatically parses it as a JSON object into a Map<String, Object>
            return (Map<String, Object>) partition;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> offset() {
            // Dirty hack to evade PicoCLI's automatic parsing for Map fields,
            // which it expects to come in key=value format
            // We declare the field with a type of Object, but a converter that
            // automatically parses it as a JSON object into a Map<String, Object>
            return (Map<String, Object>) offset;
        }
    }

    static class JsonObjectConverter implements CommandLine.ITypeConverter<Map<String, Object>> {

        private static final ObjectMapper mapper = new ObjectMapper();

        @Override
        public Map<String, Object> convert(String s) throws Exception {
            return mapper.readValue(s, new TypeReference<>() {
            });
        }

    }

    static class PatchedConnectorOffset {
        @CommandLine.ArgGroup(exclusive = false)
        SinkConnectorOffset sinkConnectorOffset;

        @CommandLine.ArgGroup(exclusive = false)
        SourceConnectorOffset sourceConnectorOffset;

        ConnectorOffset offset() {
            Map<String, Object> partition;
            Map<String, Object> offset;
            if (sinkConnectorOffset != null) {
                partition = Map.of("kafka_topic", sinkConnectorOffset.topic,
                                   "kafka_partition", sinkConnectorOffset.partition);
                offset = Map.of("kafka_offset", sinkConnectorOffset.offset);
            }
            else if (sourceConnectorOffset != null) {
                partition = sourceConnectorOffset.partition();
                offset = sourceConnectorOffset.offset();
            }
            else {
                throw new IllegalStateException("At least one of sink and source offsets should be non-null");
            }
            return new ConnectorOffset(partition, offset);
        }
    }

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    PatchedConnectorOffset patchedOffset;

    @Inject
    public PatchOffsetsCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public PatchOffsetsCommand() {
        context = new ConfigurationContext();
    }

    @Override
    public Integer call() throws JsonProcessingException {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        Version currentVersion = new Version(kafkaConnectApi.getWorkerInfo().version());

        if (!currentVersion.greaterOrEquals(requiredVersionForPatchingOffsets)) {
            spec.commandLine().getErr().println(String.format("Patching connector offsets requires at least Kafka Connect %s. Current version: %s",
                    requiredVersionForPatchingOffsets, currentVersion));
            return 1;
        }

        ConnectorOffsets connectorOffsets = new ConnectorOffsets(Collections.singletonList(patchedOffset.offset()));
        AlterResetOffsetsResponse offsets = kafkaConnectApi.patchConnectorOffsets(name, connectorOffsets);
        spec.commandLine().getOut().println(offsets.message());

        return 0;
    }

}
