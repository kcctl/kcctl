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

import java.net.URI;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import dev.morling.kccli.service.KafkaConnectApi;
import picocli.CommandLine.Command;

@Command(name = "info", description = "Displays information about the Kafka Connect cluster")
public class InfoCommand implements Runnable {

    @Override
    public void run() {
        KafkaConnectApi simpleGetApi = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:8083"))
                .build(KafkaConnectApi.class);

        System.out.print(simpleGetApi.getWorkerInfo());
    }

}
