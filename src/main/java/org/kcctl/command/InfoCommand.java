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

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.service.KafkaConnectInfo;
import org.kcctl.util.ConfigurationContext;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "info", description = "Displays information about the Kafka Connect cluster")
public class InfoCommand implements Runnable {

    private final ConfigurationContext context;

    @Spec
    CommandSpec spec;

    @Inject
    public InfoCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public InfoCommand() {
        context = new ConfigurationContext();
    }

    @Override
    public void run() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        KafkaConnectInfo workerInfo = kafkaConnectApi.getWorkerInfo();
        spec.commandLine().getOut().println("URL:               " + context.getCurrentContext().getCluster());
        spec.commandLine().getOut().println("Version:           " + workerInfo.version);
        spec.commandLine().getOut().println("Commit:            " + workerInfo.commit);
        spec.commandLine().getOut().println("Kafka Cluster ID:  " + workerInfo.kafka_cluster_id);
    }
}
