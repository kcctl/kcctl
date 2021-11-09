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

import java.net.URI;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.moditect.kcctl.service.Context;
import org.moditect.kcctl.util.ConfigurationContext;
import org.moditect.kcctl.util.Strings;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "set-context", description = "Configures the specified context with the provided arguments")
public class SetContextCommand implements Callable<Integer> {

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

    @Option(names = { "--admin-client-config" }, description = "Configuration for admin client")
    String adminClientConfig;

    @Override
    public Integer call() {
        ConfigurationContext context = new ConfigurationContext();

        Properties adminClientConfigProps;
        try {
            adminClientConfigProps = Strings.toProperties(adminClientConfig);
        }
        catch (Exception exception) {
            System.out.println("The provided admin client configuration is not valid!");
            return 1;
        }

        final var adminClientConfigMap = new HashMap<String, Object>();
        for (final String name : adminClientConfigProps.stringPropertyNames()) {
            adminClientConfigMap.put(name, adminClientConfigProps.getProperty(name));
        }

        context.setContext(contextName, new Context(URI.create(cluster), bootstrapServers, offsetTopic, username, password, adminClientConfigMap));
        System.out.println("Configured context " + contextName);

        if (!context.getCurrentContextName().equals(contextName)) {
            System.out.println("Run kcctl config use-context " + contextName + " for using this context");
        }

        return 0;
    }
}
