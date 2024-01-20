/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

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
            for (TaskState task : status.tasks()) {
                completions.add(connector + "/" + task.id());
            }
        }

        System.out.println(String.join(" ", completions));
    }
}
