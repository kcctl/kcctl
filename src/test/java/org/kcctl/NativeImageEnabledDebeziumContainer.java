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
 * The upstream {@code DebeziumContainer} connects to Kafka through a Strimzi cluster only, so this
 * connects it to a plain {@code KafkaContainer}.
 */
public class NativeImageEnabledDebeziumContainer extends DebeziumContainer {

    public NativeImageEnabledDebeziumContainer(final String containerImageName) {
        super(containerImageName);
    }

    public NativeImageEnabledDebeziumContainer(ImageFromDockerfile withFileFromClasspath) {
        super(withFileFromClasspath);
    }

    public NativeImageEnabledDebeziumContainer withKafkaBroker(final KafkaContainer kafkaContainer) {
        return withKafkaBroker(kafkaContainer.getNetwork(), kafkaContainer.getNetworkAliases().get(0) + ":9093");
    }

    public NativeImageEnabledDebeziumContainer withKafkaBroker(final Network network, final String bootstrapServers) {
        withNetwork(network);
        withEnv("BOOTSTRAP_SERVERS", bootstrapServers);
        return (NativeImageEnabledDebeziumContainer) self();
    }

    public NativeImageEnabledDebeziumContainer withNetwork(Network network) {
        super.withNetwork(network);
        return (NativeImageEnabledDebeziumContainer) self();
    }
}
