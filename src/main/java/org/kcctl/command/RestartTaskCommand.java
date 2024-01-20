/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.completion.TaskNameCompletions;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.util.ConfigurationContext;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "task", description = "Restarts the specified task")
public class RestartTaskCommand implements Runnable {

    @CommandLine.Mixin
    HelpMixin help;

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
