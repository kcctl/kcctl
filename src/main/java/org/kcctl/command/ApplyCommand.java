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

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kcctl.service.ConfigInfos;
import org.kcctl.service.KafkaConnectApi;
import org.kcctl.service.KafkaConnectException;
import org.kcctl.util.ConfigurationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import static org.kcctl.util.Colors.ANSI_RESET;
import static org.kcctl.util.Colors.ANSI_WHITE_BOLD;

@Command(name = "apply", description = "Applies the given files or the stdin content for registering or updating connectors")
public class ApplyCommand implements Callable<Integer> {
    private record ApplyConnector(String contents, Map<String, Object> config) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public Map<String, String> configMap() {
            return config.containsKey("config") ? (Map) config.get("config") : (Map) config;
        }
        String name() {
            return (String) config.get("name");
        }
        boolean isNamed() {
            return config.containsKey("name") && config.containsKey("config");
        }
    }

    // Hack : workaround. Should be `List<ApplyConnector>` instead of List.
    // But Graalvm seems to have difficulty building a native binary with ApplyConnector type record in class fields
    // Support "-f file1.json file2.json" in addition to "-f file1.json -f file2.json" in order to work smoothly with shell
    // globbing (e.g., "-f file*.json")
    @Option(names = { "-f",
            "--file" }, description = "Names of the file to apply or '-' to read from stdin. You can specify multiple filenames, if the connector names are in the files", required = true, converter = ApplyConnectorConverter.class, arity = "1..*")
    List applyConnectors;

    @Option(names = { "-n", "--name" }, description = "Name of the connector when not given within the file itself")
    String name;

    @Option(names = { "--dry-run" }, description = "Only validates the configuration")
    boolean dryRun;

    @Spec
    CommandSpec spec;

    private static final ObjectMapper mapper = new ObjectMapper();
    private final static String CONFIG_EXCEPTION = "org.apache.kafka.common.config.ConfigException: ";
    private final ConfigurationContext context;

    @Inject
    public ApplyCommand(ConfigurationContext context) {
        this.context = context;
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public ApplyCommand() {
        context = new ConfigurationContext();
    }

    private static String readFromStdin() {
        Scanner sc = new Scanner(System.in);
        StringBuilder buffer = new StringBuilder();
        while (sc.hasNextLine())
            buffer.append(sc.nextLine());
        sc.close();

        return buffer.toString();
    }

    static class ApplyConnectorConverter implements CommandLine.ITypeConverter<ApplyConnector> {
        @SuppressWarnings("unchecked")
        public ApplyConnector convert(String filename) {
            File file = new File(filename);
            String contents;
            Map<String, Object> config;
            if (file.getName().equals("-")) {
                contents = readFromStdin();
            }
            else if (!file.exists()) {
                throw new CommandLine.TypeConversionException("Given file does not exist: " + file.toPath().toAbsolutePath());
            }
            else {
                try {
                    contents = Files.readString(file.toPath());
                }
                catch (Exception e) {
                    throw new RuntimeException("Couldn't read file", e);
                }
            }

            try {
                config = mapper.readValue(contents, Map.class);
            }
            catch (JsonProcessingException e) {
                throw new RuntimeException("Can't parse Json content", e);
            }

            return new ApplyConnector(contents, config);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Integer call() throws Exception {

        validate();

        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        for (ApplyConnector applyConnector : (List<ApplyConnector>) applyConnectors) {
            int returnCode = applyOrValidateConnector(kafkaConnectApi, applyConnector);
            if (returnCode > 0)
                return returnCode;
        }

        return 0;
    }

    private void validate() {
        if (applyConnectors.size() > 1 && name != null)
            throw new CommandLine.ParameterException(spec.commandLine(),
                    "It is not possible to use -n when multiple files are given. Please provide the connector names in the connector configuration files.");
    }

    private int applyOrValidateConnector(KafkaConnectApi kafkaConnectApi, ApplyConnector applyConnector) throws Exception {
        if (dryRun) {
            return validateConfigs(kafkaConnectApi, applyConnector);
        }
        else {
            return createOrUpdateConnector(kafkaConnectApi, applyConnector);
        }
    }

    private int createOrUpdateConnector(KafkaConnectApi kafkaConnectApi, ApplyConnector applyConnector) throws Exception {
        try {
            if (applyConnector.isNamed()) {
                String connectorName = applyConnector.name();
                boolean existing = kafkaConnectApi.getConnectors().contains(connectorName);
                if (!existing) {
                    kafkaConnectApi.createConnector(applyConnector.contents);
                    spec.commandLine().getOut().println("Created connector " + connectorName);
                }
                else {
                    kafkaConnectApi.updateConnector(connectorName, mapper.writeValueAsString(applyConnector.configMap()));
                    spec.commandLine().getOut().println("Updated connector " + connectorName);
                }
            }
            else {
                if (name == null) {
                    spec.commandLine().getOut().println("Connector name must be specified either via --name or in the given file");
                    return 1;
                }

                boolean existing = kafkaConnectApi.getConnectors().contains(name);
                kafkaConnectApi.updateConnector(name, applyConnector.contents);

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
                getPlugins.call();
            }
            else {
                spec.commandLine().getOut().println(kce.getMessage());
            }

            return 1;
        }

        return 0;
    }

    private int validateConfigs(KafkaConnectApi kafkaConnectApi, ApplyConnector applyConnector) throws Exception {
        Map<String, String> connectorConfigMap = applyConnector.configMap();

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
            int errs = configInfos.errorCount();
            if (errs == 0) {
                spec.commandLine().getOut().println("The configuration is valid!");
            }
            else {
                spec.commandLine().getOut().println("The configuration is not valid! Found " + errs + " error" + ((errs != 1) ? "s" : "") + ".");
                spec.commandLine().getOut().println(ANSI_WHITE_BOLD + "Errors" + ANSI_RESET);
                for (ConfigInfos.ConfigInfo configInfo : configInfos.configs()) {
                    List<String> errors = configInfo.configValue().errors();
                    if (errors != null && !errors.isEmpty()) {
                        spec.commandLine().getOut().println("  " + configInfo.configKey().name());
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
