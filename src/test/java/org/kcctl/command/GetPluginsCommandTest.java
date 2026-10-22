/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.kcctl.IntegrationTest;
import org.kcctl.IntegrationTestProfile;
import org.kcctl.support.InjectCommandContext;
import org.kcctl.support.KcctlCommandContext;
import org.kcctl.support.OutputLines;
import org.kcctl.support.SkipIfConnectVersionIsOlderThan;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class GetPluginsCommandTest extends IntegrationTest {

    @InjectCommandContext
    KcctlCommandContext<GetPluginsCommand> context;

    /**
     * Source connectors every Debezium image ships, in the order kcctl lists them. Images add
     * further connectors over time, which the assertion tolerates.
     */
    private static final List<String> DEBEZIUM_SOURCE_CONNECTORS = List.of(
            "io.debezium.connector.db2.Db2Connector",
            "io.debezium.connector.mongodb.MongoDbConnector",
            "io.debezium.connector.mysql.MySqlConnector",
            "io.debezium.connector.oracle.OracleConnector",
            "io.debezium.connector.postgresql.PostgresConnector",
            "io.debezium.connector.sqlserver.SqlServerConnector",
            "io.debezium.connector.vitess.VitessConnector");

    private static final List<String> KAFKA_SOURCE_CONNECTORS = List.of(
            "org.apache.kafka.connect.mirror.MirrorCheckpointConnector",
            "org.apache.kafka.connect.mirror.MirrorHeartbeatConnector",
            "org.apache.kafka.connect.mirror.MirrorSourceConnector");

    @Test
    public void should_list_source_plugins() {
        context.runAndEnsureExitCodeOk("--types=source");
        List<String> lines = OutputLines.of(context.output().toString());

        assertThat(lines).startsWith("TYPE CLASS VERSION");
        assertThat(lines).allMatch(line -> line.startsWith("TYPE ") || line.startsWith("source "));
        assertThat(lines.stream().map(line -> line.split(" ")[1]))
                .containsSubsequence(Stream.concat(DEBEZIUM_SOURCE_CONNECTORS.stream(), KAFKA_SOURCE_CONNECTORS.stream()).toList());
        // The nightly image does not state its Debezium version
        if (!getDebeziumVersion().isBlank()) {
            assertThat(lines).containsSubsequence(DEBEZIUM_SOURCE_CONNECTORS.stream()
                    .map(connector -> "source " + connector + " " + getDebeziumVersion())
                    .toList());
        }
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
        String allTypes = Stream.of(GetPluginsCommand.PluginType.values()).map(t -> t.name).collect(Collectors.joining(","));
        context.runAndEnsureExitCodeOk("--types=" + allTypes);
        assertThat(context.output().toString().trim()).isEqualTo(output);
    }
}
