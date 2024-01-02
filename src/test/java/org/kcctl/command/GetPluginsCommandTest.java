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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.kcctl.IntegrationTest;
import org.kcctl.IntegrationTestProfile;
import org.kcctl.support.InjectCommandContext;
import org.kcctl.support.KcctlCommandContext;
import org.kcctl.support.SkipIfConnectVersionIsOlderThan;
import org.kcctl.util.Version;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class GetPluginsCommandTest extends IntegrationTest {

    private static final Version SPANNER_CONNECTOR_MIN_VERSION = new Version(2, 1);
    private static final Version INFORMIX_CONNECTOR_MIN_VERSION = new Version(2, 5);
    private static final Version MIRROR_MAKER_MIN_GRANULAR_VERSION = new Version(3, 2);
    private static final Version FILE_CONNECTOR_REMOVAL_MIN_VERSION = new Version(3, 2); // Also removed in 3.1.1 (see https://issues.apache.org/jira/browse/KAFKA-13748) but there's no Debezium image built off of that so 3.2.x is fine

    @InjectCommandContext
    KcctlCommandContext<GetPluginsCommand> context;

    @Test
    public void should_list_source_plugins() throws Exception {
        String debeziumVersion = getDebeziumVersion();
        String kafkaVersion = getConnectVersion();

        Version parsedDebeziumVersion = new Version(debeziumVersion);
        Version parsedKafkaVersion = new Version(kafkaVersion);

        String spannerConnectorDescription = null;
        if (parsedDebeziumVersion.greaterOrEquals(SPANNER_CONNECTOR_MIN_VERSION)) {
            spannerConnectorDescription = "source   io.debezium.connector.spanner.SpannerConnector              " + debeziumVersion;
        }

        String informixConnectorDescription = null;
        if (parsedDebeziumVersion.greaterOrEquals(INFORMIX_CONNECTOR_MIN_VERSION)) {
            informixConnectorDescription = "source   io.debezium.connector.informix.InformixConnector            " + debeziumVersion;
        }

        String mirrorMakerVersion = "1";
        if (parsedKafkaVersion.greaterOrEquals(MIRROR_MAKER_MIN_GRANULAR_VERSION)) {
            mirrorMakerVersion = kafkaVersion;
        }

        String fileConnectorDescription = "source   org.apache.kafka.connect.file.FileStreamSourceConnector     " + kafkaVersion;
        if (parsedKafkaVersion.greaterOrEquals(FILE_CONNECTOR_REMOVAL_MIN_VERSION)) {
            fileConnectorDescription = null;
        }

        String[] expectedOutput = Stream.of(
                "TYPE     CLASS                                                       VERSION",
                "source   io.debezium.connector.db2.Db2Connector                      " + debeziumVersion,
                informixConnectorDescription,
                "source   io.debezium.connector.mongodb.MongoDbConnector              " + debeziumVersion,
                "source   io.debezium.connector.mysql.MySqlConnector                  " + debeziumVersion,
                "source   io.debezium.connector.oracle.OracleConnector                " + debeziumVersion,
                "source   io.debezium.connector.postgresql.PostgresConnector          " + debeziumVersion,
                spannerConnectorDescription,
                "source   io.debezium.connector.sqlserver.SqlServerConnector          " + debeziumVersion,
                "source   io.debezium.connector.vitess.VitessConnector                " + debeziumVersion,
                fileConnectorDescription,
                "source   org.apache.kafka.connect.mirror.MirrorCheckpointConnector   " + mirrorMakerVersion,
                "source   org.apache.kafka.connect.mirror.MirrorHeartbeatConnector    " + mirrorMakerVersion,
                "source   org.apache.kafka.connect.mirror.MirrorSourceConnector       " + mirrorMakerVersion)
                .filter(Objects::nonNull)
                .toArray(String[]::new);

        context.runAndEnsureExitCodeOk("--types=source");
        assertThat(context.output().toString().trim().lines())
                .map(String::trim)
                .containsExactly(expectedOutput);
    }

    @Test
    @SkipIfConnectVersionIsOlderThan("3.2")
    public void should_filter_by_type() {
        context.runAndEnsureExitCodeOk("--types=transformation");
        assertThat(context.output().toString().trim().lines())
                .anyMatch(l -> l.startsWith(" transformation"))
                .anyMatch(l -> l.startsWith("TYPE"));
    }

    @Test
    @SkipIfConnectVersionIsOlderThan("3.2")
    public void should_filter_by_types() {
        context.runAndEnsureExitCodeOk("--types=transformation,converter");
        assertThat(context.output().toString().trim().lines())
                .anyMatch(l -> l.startsWith(" transformation"))
                .anyMatch(l -> l.startsWith(" converter"))
                .anyMatch(l -> l.startsWith("TYPE"));
    }

    @Test
    @SkipIfConnectVersionIsOlderThan("3.2")
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
