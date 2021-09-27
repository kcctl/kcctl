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
package dev.morling.kccli.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import dev.morling.kccli.service.Configuration;
import dev.morling.kccli.service.Context;

@ApplicationScoped
public class ConfigurationContext {
    private static final String CONFIG_FILE = ".kcctl";
    private final File configFile;
    private final ObjectMapper objectMapper;

    public ConfigurationContext() {
        this(new File(System.getProperty("user.home")));
    }

    public ConfigurationContext(File configDirectory) {
        this.configFile = new File(configDirectory, CONFIG_FILE);
        this.objectMapper = JsonMapper
                .builder()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .build();
    }

    public void setContext(String contextName, Context context) {
        if (!configFile.exists()) {
            tryWriteConfiguration(new Configuration(contextName).addConfigurationContext(contextName, context));

            return;
        }

        var configuration = tryReadConfiguration();

        tryWriteConfiguration(configuration.addConfigurationContext(contextName, context));
    }

    public Context getContext() {
        if (!configFile.exists()) {
            System.out.println("No configuration context has been defined, using http://localhost:8083 by default." +
                    " Run 'kcctl config set-context <context_name> --cluster=<cluster_url> [--bootstrap-servers=<broker_urls>] [--offset-topic=<offset_topic>].' to create a context.");

            return Context.defaultContext();
        }

        var configuration = tryReadConfiguration();

        return configuration.configurationContexts().get(configuration.getCurrentContext());
    }

    public Map<String, Context> getContexts() {
        Map<String, Context> contexts = new HashMap<>();
        if (!configFile.exists()) {
            System.out.println("No configuration context has been defined, using http://localhost:8083 by default." +
                    " Run 'kcctl config set-context <context_name> --cluster=<cluster_url> [--bootstrap-servers=<broker_urls>] [--offset-topic=<offset_topic>].' to create a context.");

            return contexts;
        }

        var configuration = tryReadConfiguration();
        return configuration.configurationContexts();
    }

    private Configuration tryReadConfiguration() {
        try {
            return objectMapper.readValue(configFile, Configuration.class);
        }
        catch (IOException e) {
            throw new RuntimeException("Couldn't read configuration file ~/" + CONFIG_FILE + ". If you are using the legacy," +
                    "property-based configuration format, please delete the old .kcctl file and create a new one by " +
                    "running 'kcctl config set-context <context_name> --cluster=<cluster_url> [--bootstrap-servers=<broker_urls>] [--offset-topic=<offset_topic>]. ", e);
        }
    }

    private void tryWriteConfiguration(Configuration configuration) {
        try {
            objectMapper.writeValue(configFile, configuration);
        }
        catch (IOException e) {
            throw new RuntimeException("Couldn't write configuration file " + configFile + ". If you are using the legacy," +
                    "property-based configuration format, please delete the old .kcctl file and create a new one by " +
                    "running 'kcctl config set-context <context_name> --cluster=<cluster_url> [--bootstrap-servers=<broker_urls>] [--offset-topic=<offset_topic>]. ", e);
        }
    }
}
