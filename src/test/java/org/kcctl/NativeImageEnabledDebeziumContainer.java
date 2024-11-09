/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl;

import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.kafka.KafkaContainer;

import io.debezium.testing.testcontainers.DebeziumContainer;

/**
 * The upstream {@code DebeziumContainer} only supports the legacy {@code KafkaContainer},
 * so we're adding support for the new one here for now.
 */
public class NativeImageEnabledDebeziumContainer extends DebeziumContainer {

    public NativeImageEnabledDebeziumContainer(final String containerImageName) {
        super(containerImageName);
    }

    public NativeImageEnabledDebeziumContainer(ImageFromDockerfile withFileFromClasspath) {
        super(withFileFromClasspath);
    }

    public NativeImageEnabledDebeziumContainer withKafka(final KafkaContainer kafkaContainer) {
        return withKafka(kafkaContainer.getNetwork(), kafkaContainer.getNetworkAliases().get(0) + ":9093");
    }

    public NativeImageEnabledDebeziumContainer withKafka(final Network network, final String bootstrapServers) {
        super.withKafka(network, bootstrapServers);
        return (NativeImageEnabledDebeziumContainer) self();
    }

    public NativeImageEnabledDebeziumContainer withNetwork(Network network) {
        super.withNetwork(network);
        return (NativeImageEnabledDebeziumContainer) self();
    }
}
