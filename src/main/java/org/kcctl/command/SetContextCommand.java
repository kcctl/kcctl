/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.kcctl.service.Context;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Strings;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "set-context", description = "Configures the specified context with the provided arguments")
public class SetContextCommand implements Callable<Integer> {

    @CommandLine.Mixin
    HelpMixin help;

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
