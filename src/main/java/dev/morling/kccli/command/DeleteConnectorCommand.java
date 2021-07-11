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

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import dev.morling.kccli.service.KafkaConnectApi;
import dev.morling.kccli.util.ConfigurationContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "delete", description = "Deletes the specified connector")
public class DeleteConnectorCommand implements Runnable {

    @Inject
    ConfigurationContext context;

    @Parameters(paramLabel = "CONNECTOR NAME", description = "Name of the connector") // , completionCandidates = DummyCompletions.class)
    String name;

    @Override
    public void run() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCluster())
                .build(KafkaConnectApi.class);

        kafkaConnectApi.deleteConnector(name);
        System.out.println("Deleted connector " + name);
    }
}
