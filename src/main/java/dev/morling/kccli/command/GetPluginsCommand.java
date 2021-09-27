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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import dev.morling.kccli.service.ConnectorPlugin;
import dev.morling.kccli.service.KafkaConnectApi;
import dev.morling.kccli.util.ConfigurationContext;
import picocli.CommandLine.Command;

@Command(name = "plugins", description = "Displays information about available connector plug-ins")
public class GetPluginsCommand implements Runnable {

    @Inject
    ConfigurationContext context;

    @Override
    public void run() {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCurrentContext().getCluster())
                .build(KafkaConnectApi.class);

        List<ConnectorPlugin> connectorPlugins = kafkaConnectApi.getConnectorPlugins();
        Collections.sort(connectorPlugins, (c1, c2) -> -c1.type.compareTo(c2.type));

        System.out.println();
        System.out.println(AsciiTable.getTable(AsciiTable.NO_BORDERS, connectorPlugins, Arrays.asList(
                new Column().header("TYPE").dataAlign(HorizontalAlign.LEFT).with(plugin -> plugin.type),
                new Column().header(" CLASS").dataAlign(HorizontalAlign.LEFT).with(plugin -> " " + plugin.clazz),
                new Column().header(" VERSION").dataAlign(HorizontalAlign.LEFT).with(plugin -> " " + plugin.version))));
        System.out.println();
    }
}
