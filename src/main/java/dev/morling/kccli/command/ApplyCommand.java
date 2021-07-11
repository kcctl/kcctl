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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.morling.kccli.service.KafkaConnectApi;
import dev.morling.kccli.util.ConfigurationContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "apply", description = "Applies the given file for registering or updating a connector")
public class ApplyCommand implements Callable<Integer> {

    @Inject
    ConfigurationContext context;

    @Option(names = { "-f", "--file" }, description = "Name of the file to apply", required = true)
    File file;

    @Option(names = { "-n", "--name" }, description = "Name of the connector when not given within the file itself")
    String name;

    @Override
    public Integer call() throws Exception {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCluster())
                .build(KafkaConnectApi.class);

        if (!file.exists()) {
            System.out.println("Given file does not exist: " + file.toPath().toAbsolutePath());
            return 1;
        }

        String contents = null;
        try {
            contents = Files.readString(file.toPath());
        }
        catch (IOException e) {
            throw new RuntimeException("Couldn't read file", e);
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> config = mapper.readValue(contents, Map.class);

        if (config.containsKey("name") && config.containsKey("config")) {
            boolean existing = kafkaConnectApi.getConnectors().contains(config.get("name"));
            if (!existing) {
                kafkaConnectApi.createConnector(contents);
                System.out.println("Created connector " + config.get("name"));
            }
            else {
                kafkaConnectApi.updateConnector(name, mapper.writeValueAsString(config.get("config")));
                System.out.println("Updated connector " + config.get("name"));
            }
        }
        else {
            if (name == null) {
                System.out.println("Connector name must be specified either via --name or in the given file");
                return 1;
            }

            boolean existing = kafkaConnectApi.getConnectors().contains(config.get("name"));
            kafkaConnectApi.updateConnector(name, contents);

            if (existing) {
                System.out.println("Created connector " + config.get("name"));
            }
            else {
                System.out.println("Updated connector " + config.get("name"));
            }
        }

        return 0;
    }
}
