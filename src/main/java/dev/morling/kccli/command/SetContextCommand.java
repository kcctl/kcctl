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

import dev.morling.kccli.service.Context;
import dev.morling.kccli.util.ConfigurationContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "set-context", description = "Set the context to cluster provided in arguments")
public class SetContextCommand implements Runnable {
    @Parameters(index = "0", description = "Context name")
    String contextName;

    @Option(names = { "--cluster" }, defaultValue = "http://localhost:8083", description = "URL of the Kafka Connect cluster to connect to")
    String cluster;

    @Option(names = { "--bootstrap-servers" }, defaultValue = "localhost:9092", description = "Comma-separated list of Kafka broker URLs")
    String bootstrapServers;

    @Option(names = { "--offset-topic" }, description = "Name of the offset topic")
    String offsetTopic;

    @Option(names = { "--username" }, description = "Username for basic authentication")
    String username;

    @Option(names = { "--password" }, description = "Password for basic authentication")
    String password;

    @Override
    public void run() {
        ConfigurationContext context = new ConfigurationContext();
        context.setContext(contextName, new Context(URI.create(cluster), bootstrapServers, offsetTopic, username, password));
        System.out.println("Using context " + contextName);
    }
}
