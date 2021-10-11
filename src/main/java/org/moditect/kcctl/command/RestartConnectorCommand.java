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
package org.moditect.kcctl.command;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.moditect.kcctl.completion.ConnectorNameCompletions;
import org.moditect.kcctl.service.KafkaConnectApi;
import org.moditect.kcctl.util.ConfigurationContext;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "connector", description = "Restarts the specified connector")
public class RestartConnectorCommand implements Runnable {

    @Inject
    ConfigurationContext context;

    @Parameters(paramLabel = "NAME", description = "Name of the connector (e.g. 'my-connector')", completionCandidates = ConnectorNameCompletions.class)
    String name;

    @Override
    public void run() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        kafkaConnectApi.restartConnector(name);
        System.out.println("Restarted connector " + name);
    }
}
