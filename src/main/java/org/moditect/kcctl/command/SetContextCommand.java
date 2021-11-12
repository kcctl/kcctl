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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
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

    @Option(names = { "-o", "--client-config" }, description = "Configuration for client")
    String[] clientConfigs;

    @Option(names = { "-f", "--client-config-file" }, description = "Configuration file for client")
    String clientConfigFile;

    @Override
    public Integer call() throws FileNotFoundException, IOException {
        ConfigurationContext context = new ConfigurationContext();

        Properties clientConfigProps = new Properties();

        if (!Strings.isBlank(clientConfigFile)) {
            final String expandedPath = Paths.get(clientConfigFile).toAbsolutePath().toString();
            clientConfigProps.load(new FileReader(expandedPath));
        }

        if (this.clientConfigs != null) {
            for (final String clientConfigString : this.clientConfigs) {
                try {
                    clientConfigProps.putAll(Strings.toProperties(clientConfigString));
                }
                catch (Exception exception) {
                    System.out.println("The provided client configuration is not valid!");
                    return 1;
                }
            }
        }

        final var clientConfigMap = new HashMap<String, Object>();
        for (final String name : clientConfigProps.stringPropertyNames()) {
            clientConfigMap.put(name, clientConfigProps.getProperty(name));
        }

        context.setContext(contextName, new Context(URI.create(cluster), bootstrapServers, offsetTopic, username, password, clientConfigMap));
        System.out.println("Configured context " + contextName);

        if (!context.getCurrentContextName().equals(contextName)) {
            System.out.println("Run kcctl config use-context " + contextName + " for using this context");
        }

        return 0;
    }

}
