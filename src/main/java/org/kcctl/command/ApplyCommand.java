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

import static org.kcctl.util.Colors.ANSI_RESET;
import static org.kcctl.util.Colors.ANSI_WHITE_BOLD;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.service.ConfigInfos;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.service.KafkaConnectException;
import org.kcctl.util.ConfigurationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "apply", description = "Applies the given file or the stdin content for registering or updating a connector")
public class ApplyCommand implements Callable<Integer> {

    @Option(names = { "-f", "--file" }, description = "Name of the file to apply or '-' to read from stdin", required = true)
    File file;

    @Option(names = { "-n", "--name" }, description = "Name of the connector when not given within the file itself")
    String name;

    @Option(names = { "--dry-run" }, description = "Only validates the configuration")
    boolean dryRun;

    @Spec
    CommandSpec spec;

    private final static String CONFIG_EXCEPTION = "org.apache.kafka.common.config.ConfigException: ";
    private final ConfigurationContext context;
    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    public ApplyCommand(ConfigurationContext context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Integer call() throws Exception {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        String contents;
        if (file.getName().equals("-")) {
            contents = readFromStdin();
        }
        else if (!file.exists()) {
            spec.commandLine().getOut().println("Given file does not exist: " + file.toPath().toAbsolutePath());
            return 1;
        }
        else {
            try {
                contents = Files.readString(file.toPath());
            }
            catch (IOException e) {
                throw new RuntimeException("Couldn't read file", e);
            }
        }

        Map<String, Object> config = mapper.readValue(contents, Map.class);

        if (dryRun) {
            return validateConfigs(kafkaConnectApi, config);
        }
        else {
            return createOrUpdateConnector(kafkaConnectApi, contents, config);
        }
    }

    private String readFromStdin() {
        Scanner sc = new Scanner(System.in);
        StringBuilder buffer = new StringBuilder();
        while (sc.hasNextLine())
            buffer.append(sc.nextLine());
        sc.close();
        return buffer.toString();
    }

    private int createOrUpdateConnector(KafkaConnectApi kafkaConnectApi, String contents, Map<String, Object> config) throws Exception {
        try {
            if (config.containsKey("name") && config.containsKey("config")) {
                String connectorName = (String) config.get("name");
                boolean existing = kafkaConnectApi.getConnectors().contains(connectorName);
                if (!existing) {
                    kafkaConnectApi.createConnector(contents);
                    spec.commandLine().getOut().println("Created connector " + connectorName);
                }
                else {
                    kafkaConnectApi.updateConnector(connectorName, mapper.writeValueAsString(config.get("config")));
                    spec.commandLine().getOut().println("Updated connector " + connectorName);
                }
            }
            else {
                if (name == null) {
                    spec.commandLine().getOut().println("Connector name must be specified either via --name or in the given file");
                    return 1;
                }

                boolean existing = kafkaConnectApi.getConnectors().contains(name);
                kafkaConnectApi.updateConnector(name, contents);

                if (!existing) {
                    spec.commandLine().getOut().println("Created connector " + name);
                }
                else {
                    spec.commandLine().getOut().println("Updated connector " + name);
                }
            }
        }
        catch (KafkaConnectException kce) {
            if (kce.getMessage().startsWith("Failed to find any class that implements Connector")) {

                spec.commandLine().getOut().println("Specified class isn't a valid connector type. The following connector type(s) are available:");

                GetPluginsCommand getPlugins = new GetPluginsCommand(context);
                getPlugins.run();
            }
            else {
                spec.commandLine().getOut().println(kce.getMessage());
            }

            return 1;
        }

        return 0;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private int validateConfigs(KafkaConnectApi kafkaConnectApi, Map<String, Object> config) throws Exception {
        Map<String, String> connectorConfigMap = (config.containsKey("config"))
                ? (Map) config.get("config")
                : (Map) config;

        if (!connectorConfigMap.containsKey("connector.class")) {
            spec.commandLine().getOut().println("The configuration must contain the 'connector.class' field.");
            return 1;
        }
        // In order to start a connector, "name" is not required within "config". However, when validating a
        // configuration it is! So injecting a placeholder to make sure this does not fail validation.
        connectorConfigMap.putIfAbsent("name", "dummy-name");

        String clazz = connectorConfigMap.get("connector.class");
        String pluginName = clazz.substring(clazz.lastIndexOf('.') + 1);
        try {
            ConfigInfos configInfos = kafkaConnectApi.validateConfig(pluginName, mapper.writeValueAsString(connectorConfigMap));
            int errs = configInfos.errorCount;
            if (errs == 0) {
                spec.commandLine().getOut().println("The configuration is valid!");
            }
            else {
                spec.commandLine().getOut().println("The configuration is not valid! Found " + errs + " error" + ((errs != 1) ? "s" : "") + ".");
                spec.commandLine().getOut().println(ANSI_WHITE_BOLD + "Errors" + ANSI_RESET);
                for (ConfigInfos.ConfigInfo configInfo : configInfos.configs) {
                    List<String> errors = configInfo.configValue.errors;
                    if (errors != null && !errors.isEmpty()) {
                        spec.commandLine().getOut().println("  " + configInfo.configKey.name);
                        for (String error : errors) {
                            spec.commandLine().getOut().println("    " + error);
                        }
                    }
                }
                return 1;
            }
        }
        catch (KafkaConnectException kce) {
            if (kce.getMessage().startsWith(CONFIG_EXCEPTION)) {
                spec.commandLine().getOut().println("The configuration is not valid! Found 1 error.");
                spec.commandLine().getOut().println(ANSI_WHITE_BOLD + "Errors" + ANSI_RESET);
                spec.commandLine().getOut().println("  " + kce.getMessage().replace(CONFIG_EXCEPTION, ""));
                return 1;
            }
            else {
                throw kce;
            }
        }

        return 0;
    }
}
