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
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class GetPluginsCommandTest extends IntegrationTest {

    @InjectCommandContext
    KcctlCommandContext<GetPluginsCommand> context;

    @Test
    public void should_print_info() {
        String debeziumVersion = ContainerImageVersions.getStableVersion("debezium/connect");

        int exitCode = context.commandLine().execute();
        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(context.output().toString().trim().lines())
                .map(String::trim)
                .containsExactly(
                        "TYPE     CLASS                                                       VERSION",
                        "source   io.debezium.connector.db2.Db2Connector                      " + debeziumVersion,
                        "source   io.debezium.connector.mongodb.MongoDbConnector              " + debeziumVersion,
                        "source   io.debezium.connector.mysql.MySqlConnector                  " + debeziumVersion,
                        "source   io.debezium.connector.oracle.OracleConnector                " + debeziumVersion,
                        "source   io.debezium.connector.postgresql.PostgresConnector          " + debeziumVersion,
                        "source   io.debezium.connector.sqlserver.SqlServerConnector          " + debeziumVersion,
                        "source   io.debezium.connector.vitess.VitessConnector                " + debeziumVersion,
                        "source   org.apache.kafka.connect.file.FileStreamSourceConnector     3.1.0",
                        "source   org.apache.kafka.connect.mirror.MirrorCheckpointConnector   1",
                        "source   org.apache.kafka.connect.mirror.MirrorHeartbeatConnector    1",
                        "source   org.apache.kafka.connect.mirror.MirrorSourceConnector       1",
                        "sink     org.apache.kafka.connect.file.FileStreamSinkConnector       3.1.0");
    }
}
