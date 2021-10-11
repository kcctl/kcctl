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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.moditect.kcctl.service.ConfigInfos;
import org.moditect.kcctl.service.KafkaConnectApi;
import org.moditect.kcctl.service.KafkaConnectException;
import org.moditect.kcctl.util.ConfigurationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static org.moditect.kcctl.util.Colors.ANSI_RESET;
import static org.moditect.kcctl.util.Colors.ANSI_WHITE_BOLD;

@Command(name = "apply", description = "Applies the given file for registering or updating a connector")
public class ApplyCommand implements Callable<Integer> {

    @Inject
    ConfigurationContext context;

    @Option(names = { "-f", "--file" }, description = "Name of the file to apply", required = true)
    File file;

    @Option(names = { "-n", "--name" }, description = "Name of the connector when not given within the file itself")
    String name;

    @Option(names = { "--dry-run" }, description = "Only validates the configuration")
    boolean dryRun;

    private final static String CONFIG_EXCEPTION = "org.apache.kafka.common.config.ConfigException: ";
    private final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    @Override
    public Integer call() throws Exception {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        if (!file.exists()) {
            System.out.println("Given file does not exist: " + file.toPath().toAbsolutePath());
            return 1;
        }

        String contents;
        try {
            contents = Files.readString(file.toPath());
        }
        catch (IOException e) {
            throw new RuntimeException("Couldn't read file", e);
        }

        Map<String, Object> config = mapper.readValue(contents, Map.class);

        if (dryRun) {
            return validateConfigs(kafkaConnectApi, config);
        }
        else {
            return createOrUpdateConnector(kafkaConnectApi, contents, config);
        }
    }

    private int createOrUpdateConnector(KafkaConnectApi kafkaConnectApi, String contents, Map<String, Object> config) throws Exception {
        try {
            if (config.containsKey("name") && config.containsKey("config")) {
                String connectorName = (String) config.get("name");
                boolean existing = kafkaConnectApi.getConnectors().contains(connectorName);
                if (!existing) {
                    kafkaConnectApi.createConnector(contents);
                    System.out.println("Created connector " + connectorName);
                }
                else {
                    kafkaConnectApi.updateConnector(connectorName, mapper.writeValueAsString(config.get("config")));
                    System.out.println("Updated connector " + connectorName);
                }
            }
            else {
                if (name == null) {
                    System.out.println("Connector name must be specified either via --name or in the given file");
                    return 1;
                }

                boolean existing = kafkaConnectApi.getConnectors().contains(name);
                kafkaConnectApi.updateConnector(name, contents);

                if (!existing) {
                    System.out.println("Created connector " + name);
                }
                else {
                    System.out.println("Updated connector " + name);
                }
            }
        }
        catch (KafkaConnectException kce) {
            if (kce.getMessage().startsWith("Failed to find any class that implements Connector")) {

                System.out.println("Specified class isn't a valid connector type. The following connector type(s) are available:");

                GetPluginsCommand getPlugins = new GetPluginsCommand();
                getPlugins.context = context;
                getPlugins.run();
            }
            else {
                System.out.println(kce.getMessage());
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
            System.out.println("The configuration must contain the 'connector.class' field.");
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
                System.out.println("The configuration is valid!");
            }
            else {
                System.out.println("The configuration is not valid! Found " + errs + " error" + ((errs != 1) ? "s" : "") + ".");
                System.out.println(ANSI_WHITE_BOLD + "Errors" + ANSI_RESET);
                for (ConfigInfos.ConfigInfo configInfo : configInfos.configs) {
                    List<String> errors = configInfo.configValue.errors;
                    if (errors != null && !errors.isEmpty()) {
                        System.out.println("  " + configInfo.configKey.name);
                        for (String error : errors) {
                            System.out.println("    " + error);
                        }
                    }
                }
                return 1;
            }
        }
        catch (KafkaConnectException kce) {
            if (kce.getMessage().startsWith(CONFIG_EXCEPTION)) {
                System.out.println("The configuration is not valid! Found 1 error.");
                System.out.println(ANSI_WHITE_BOLD + "Errors" + ANSI_RESET);
                System.out.println("  " + kce.getMessage().replace(CONFIG_EXCEPTION, ""));
                return 1;
            }
            else {
                throw kce;
            }
        }

        return 0;
    }
}
