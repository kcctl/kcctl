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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.service.ConnectorStatusInfo;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.service.TaskState;
import org.kcctl.util.ConfigurationContext;

import picocli.CommandLine.Command;

@Command(name = "task-name-completions", hidden = true)
public class TaskNamesCompletionCandidateCommand implements Runnable {

    @Inject
    ConfigurationContext context;

    @Override
    public void run() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        List<String> connectors = kafkaConnectApi.getConnectors();
        List<String> completions = new ArrayList<>();

        for (String connector : connectors) {
            ConnectorStatusInfo status = kafkaConnectApi.getConnectorStatus(connector);
            for (TaskState task : status.tasks) {
                completions.add(connector + "/" + task.id());
            }
        }

        System.out.println(String.join(" ", completions));
    }
}
