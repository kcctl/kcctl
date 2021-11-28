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
package org.kcctl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.kcctl.util.ConfigurationContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import io.debezium.testing.testcontainers.DebeziumContainer;

@Tag("integration-test")
public abstract class IntegrationTest {

    protected static final Network network = Network.newNetwork();

    protected static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"))
            .withNetwork(network);

    protected static DebeziumContainer kafkaConnect = new DebeziumContainer(DockerImageName.parse("debezium/connect:1.7.1.Final"))
            .withNetwork(network)
            .withKafka(kafka)
            .dependsOn(kafka);

    protected ConfigurationContext configurationContext;

    @BeforeAll
    static void startContainers() {
        Startables.deepStart(Stream.of(kafka, kafkaConnect)).join();
    }

    @BeforeEach
    void setupContext(@TempDir File tempDir) throws IOException {
        var configFile = tempDir.toPath().resolve(".kcctl");

        Files.writeString(configFile, String.format(
                "{ \"currentContext\": \"local\", \"local\": { \"cluster\": \"%s\", \"username\": \"testuser\", \"password\": \"testpassword\" }}",
                kafkaConnect.getTarget())
        );

        configurationContext = new ConfigurationContext(tempDir);
    }
}
