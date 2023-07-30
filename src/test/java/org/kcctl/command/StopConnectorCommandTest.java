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

import java.time.Duration;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.kcctl.IntegrationTest;
import org.kcctl.IntegrationTestProfile;
import org.kcctl.support.InjectCommandContext;
import org.kcctl.support.KcctlCommandContext;
import org.kcctl.support.SkipIfConnectVersionIsOlderThan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.debezium.testing.testcontainers.Connector;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SkipIfConnectVersionIsOlderThan("3.5")
class StopConnectorCommandTest extends IntegrationTest {

    private static final long CONNECTOR_STOP_TIMEOUT_SECONDS = 10;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @InjectCommandContext
    KcctlCommandContext<StopConnectorCommand> context;

    @Test
    public void should_stop_connector() {
        registerTestConnectors("test1", "test2", "test3");
        // Make sure that the connectors actually start before we try to stop them
        ensureConnectorsRunning("test1", "test2", "test3");

        // Then stop some but not all of the connectors
        context.runAndEnsureExitCodeOk("test1", "test2");
        assertThat(context.output().toString()).contains("Stopped connector test1", "Stopped connector test2");
        assertThat(context.output().toString()).doesNotContain("Stopped connector test3");

        // Ensure that those connectors are actually stopped
        ensureConnectorsStopped("test1", "test2");
        // And ensure that the third connector is still running
        ensureConnectorsRunning("test3");
    }

    @Test
    public void should_stop_connector_with_regexp() {
        registerTestConnectors("match-1-test", "match-2-test", "nomatch-3-test");
        // Make sure that the connectors actually start before we try to stop them
        ensureConnectorsRunning("match-1-test", "match-2-test", "nomatch-3-test");

        // Then stop some but not all of the connectors
        context.runAndEnsureExitCodeOk("--reg-exp", "match-\\d-test");
        assertThat(context.output().toString()).contains("Stopped connector match-1-test", "Stopped connector match-2-test");
        assertThat(context.output().toString()).doesNotContain("Stopped connector nomatch-3-test");

        // Ensure that those connectors are actually stopped
        ensureConnectorsStopped("match-1-test", "match-2-test");
        // And ensure that the third connector is still running
        ensureConnectorsRunning("nomatch-3-test");
    }

    @Test
    public void should_stop_only_once() {
        registerTestConnector("test1");
        ensureConnectorsRunning("test1");

        context.runAndEnsureExitCodeOk("test1", "test1", "test1");
        assertThat(context.output().toString()).containsOnlyOnce("Stopped connector test1");

        ensureConnectorsStopped("test1");
    }

    @Test
    public void should_stop_only_once_with_regexp() {
        registerTestConnectors("match-1-test", "match-2-test", "nomatch-3-test");
        // Make sure that the connectors actually start before we try to stop them
        ensureConnectorsRunning("match-1-test", "match-2-test", "nomatch-3-test");

        // Then stop some but not all of the connectors
        context.runAndEnsureExitCodeOk("--reg-exp", "match-\\d-test", "match-.*", "match-1-test");
        assertThat(context.output().toString()).containsOnlyOnce("Stopped connector match-1-test");
        assertThat(context.output().toString()).containsOnlyOnce("Stopped connector match-2-test");
        assertThat(context.output().toString()).doesNotContain("Stopped connector nomatch-3-test");

        // Ensure that those connectors are actually stopped
        ensureConnectorsStopped("match-1-test", "match-2-test");
        // And ensure that the third connector is still running
        ensureConnectorsRunning("nomatch-3-test");
    }

    private void ensureConnectorsRunning(String... connectors) {
        for (String connector : connectors) {
            // Make sure the connector and one task have actually started
            kafkaConnect.ensureConnectorState(connector, Connector.State.RUNNING);
            kafkaConnect.ensureConnectorTaskState(connector, 0, Connector.State.RUNNING);
        }
    }

    // TODO: Replace this with kafkaConnect.ensureConnectorState if/when
    // the Debezium testing library is updated with support for the STOPPED state
    // (https://github.com/debezium/debezium/pull/4709)
    private void ensureConnectorsStopped(String... connectors) {
        assertThat(connectors).isNotEmpty();
        await()
                .atMost(Duration.ofSeconds(CONNECTOR_STOP_TIMEOUT_SECONDS))
                .until(() -> {
                    for (String connector : connectors) {
                        Request request = new Request.Builder().url(kafkaConnect.getConnectorStatusUri(connector)).build();
                        try (Response response = httpClient.newCall(request).execute()) {
                            assertThat(response.isSuccessful()).isTrue();
                            try (ResponseBody responseBody = response.body()) {
                                assertThat(responseBody).isNotNull();
                                ObjectNode parsedObject = (ObjectNode) mapper.readTree(responseBody.string());
                                // We don't check for an empty set of tasks, since that's the responsibility of the Kafka Connect
                                // runtime. All that we check for is acknowledgment by the runtime that our request to stop the
                                // connector was received; if some tasks haven't been shut down, that's a problem with the runtime,
                                // not kcctl
                                return "STOPPED".equals(parsedObject.get("connector").get("state").asText());
                            }
                        }
                    }
                    // Should never happen; we check for an empty connectors list above
                    throw new IllegalArgumentException("Connectors list cannot be empty");
                });
    }

}
