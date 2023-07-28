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

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.kcctl.IntegrationTest;
import org.kcctl.IntegrationTestProfile;
import org.kcctl.support.InjectCommandContext;
import org.kcctl.support.KcctlCommandContext;

import io.debezium.util.ContainerImageVersions;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class GetPluginsCommandTest extends IntegrationTest {

    @InjectCommandContext
    KcctlCommandContext<GetPluginsCommand> context;

    @Test
    public void should_list_source_plugins() throws Exception {
        String debeziumVersion = ContainerImageVersions.getStableVersion("debezium/connect");
        String kafkaVersion = getConnectVersion();

        context.runAndEnsureExitCodeOk("--types=source");
        assertThat(context.output().toString().trim().lines())
                .map(String::trim)
                .containsExactly(
                        "TYPE     CLASS                                                       VERSION",
                        "source   io.debezium.connector.db2.Db2Connector                      " + debeziumVersion,
                        "source   io.debezium.connector.mongodb.MongoDbConnector              " + debeziumVersion,
                        "source   io.debezium.connector.mysql.MySqlConnector                  " + debeziumVersion,
                        "source   io.debezium.connector.oracle.OracleConnector                " + debeziumVersion,
                        "source   io.debezium.connector.postgresql.PostgresConnector          " + debeziumVersion,
                        "source   io.debezium.connector.spanner.SpannerConnector              " + debeziumVersion,
                        "source   io.debezium.connector.sqlserver.SqlServerConnector          " + debeziumVersion,
                        "source   io.debezium.connector.vitess.VitessConnector                " + debeziumVersion,
                        "source   org.apache.kafka.connect.mirror.MirrorCheckpointConnector   " + kafkaVersion,
                        "source   org.apache.kafka.connect.mirror.MirrorHeartbeatConnector    " + kafkaVersion,
                        "source   org.apache.kafka.connect.mirror.MirrorSourceConnector       " + kafkaVersion);
    }

    @Test
    public void should_filter_by_type() {
        context.runAndEnsureExitCodeOk("--types=transformation");
        assertThat(context.output().toString().trim().lines())
                .anyMatch(l -> l.startsWith(" transformation"))
                .anyMatch(l -> l.startsWith("TYPE"));
    }

    @Test
    public void should_filter_by_types() {
        context.runAndEnsureExitCodeOk("--types=transformation,converter");
        assertThat(context.output().toString().trim().lines())
                .anyMatch(l -> l.startsWith(" transformation"))
                .anyMatch(l -> l.startsWith(" converter"))
                .anyMatch(l -> l.startsWith("TYPE"));
    }

    @Test
    public void should_list_all_types() {
        context.runAndEnsureExitCodeOk();
        String output = context.output().toString().trim();
        assertThat(output.lines())
                .anyMatch(l -> l.startsWith(" source"))
                // The debezium connect image does not currently contain any sink connectors
                .anyMatch(l -> l.startsWith(" transformation"))
                .anyMatch(l -> l.startsWith(" converter"))
                .anyMatch(l -> l.startsWith(" header_converter"))
                .anyMatch(l -> l.startsWith(" predicate"))
                .anyMatch(l -> l.startsWith("TYPE"));

        context.output().getBuffer().setLength(0);
        String allTypes = Arrays.asList(GetPluginsCommand.PluginType.values()).stream().map(t -> t.name).collect(Collectors.joining(","));
        context.runAndEnsureExitCodeOk("--types=" + allTypes);
        assertThat(context.output().toString().trim()).isEqualTo(output);
    }
}
