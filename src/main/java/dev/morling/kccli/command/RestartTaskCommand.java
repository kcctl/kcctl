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

import dev.morling.kccli.completion.TaskNameCompletions;
import dev.morling.kccli.service.KafkaConnectApi;
import dev.morling.kccli.util.ConfigurationContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "task", description = "Restarts the specified connector or task")
public class RestartTaskCommand implements Runnable {

    @Inject
    ConfigurationContext context;

    @Parameters(paramLabel = "NAME", description = "Name of the task (e.g. 'my-connector/0')", completionCandidates = TaskNameCompletions.class)
    String name;

    @Override
    public void run() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        String[] parts = name.split("\\/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid connector/task name, expecting format 'connector-name/task-id");
        }

        kafkaConnectApi.restartTask(parts[0], parts[1]);
        System.out.println("Restarted task " + name);
    }
}
