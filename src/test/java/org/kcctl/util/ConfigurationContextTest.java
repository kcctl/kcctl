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
package org.kcctl.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kcctl.service.Context;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayNameGeneration(ReplaceUnderscores.class)
class ConfigurationContextTest {

    @TempDir
    File tempDir;

    @Nested
    class SetConfiguration {
        @MethodSource("org.kcctl.util.ConfigurationContextTest#setConfigurationArguments")
        @ParameterizedTest
        void should_create_a_new_configuration_when_no_configuration_exists(String contextName, Context context, String expectedConfiguration) throws IOException {
            new ConfigurationContext(tempDir).setContext(
                    contextName,
                    context);

            var actualConfiguration = Files.readString(new File(tempDir, ".kcctl").toPath());

            assertThatJson(actualConfiguration).isEqualTo(expectedConfiguration);
        }

        @Test
        void should_add_a_new_context_when_a_configuration_already_exists() throws IOException {
            var configFile = tempDir.toPath().resolve(".kcctl");

            Files.writeString(configFile, "{ \"currentContext\": \"local\", \"local\": { \"cluster\": \"http://localhost:8083\" }}");

            new ConfigurationContext(tempDir).setContext(
                    "preprod",
                    new Context(URI.create("http://preprod:8083"), null, null, null, null, null));

            var actualConfiguration = Files.readString(configFile);

            assertThatJson(actualConfiguration)
                    .isEqualTo("{ 'currentContext': 'local', 'local': { 'cluster': 'http://localhost:8083' }, 'preprod': { 'cluster': 'http://preprod:8083'}}");
        }

        @Test
        void should_use_specified_context() throws IOException {
            var configFile = tempDir.toPath().resolve(".kcctl");

            Files.writeString(configFile, "{ \"currentContext\": \"local\", \"local\": { \"cluster\": \"http://localhost:8083\" }}");

            new ConfigurationContext(tempDir).setContext(
                    "preprod",
                    new Context(URI.create("http://preprod:8083"), null, null, null, null, null));

            var actualConfiguration = Files.readString(configFile);

            assertThatJson(actualConfiguration)
                    .isEqualTo("{ 'currentContext': 'local', 'local': { 'cluster': 'http://localhost:8083' }, 'preprod': { 'cluster': 'http://preprod:8083'}}");

            new ConfigurationContext(tempDir).setCurrentContext("preprod");

            actualConfiguration = Files.readString(configFile);

            assertThatJson(actualConfiguration)
                    .isEqualTo("{ 'currentContext': 'preprod', 'local': { 'cluster': 'http://localhost:8083' }, 'preprod': { 'cluster': 'http://preprod:8083'}}");
        }
    }

    @Nested
    class GetCluster {
        @Test
        void should_return_the_cluster_url_for_the_current_context() throws IOException {
            var configFile = tempDir.toPath().resolve(".kcctl");

            Files.writeString(configFile, "{ \"currentContext\": \"preprod\", \"preprod\": { \"cluster\": \"http://preprod:8083\" }}");

            assertThat(new ConfigurationContext(tempDir).getCurrentContext().getCluster()).isEqualTo(URI.create("http://preprod:8083"));
        }

        @Test
        void should_return_the_the_cluster_url_for_the_preprod_context() throws IOException {
            var configFile = tempDir.toPath().resolve(".kcctl");

            Files.writeString(configFile,
                    "{ \"currentContext\": \"prod\", \"preprod\": { \"cluster\": \"http://preprod:8083\" }, \"prod\": { \"cluster\": \"http://prod:8083\" }}");

            assertThat(new ConfigurationContext(tempDir).getContext("preprod").getCluster()).isEqualTo(URI.create("http://preprod:8083"));
        }
    }

    @Nested
    class GetUsername {
        @Test
        void should_return_the_username_for_the_current_context() throws IOException {
            var configFile = tempDir.toPath().resolve(".kcctl");

            Files.writeString(configFile, "{ \"currentContext\": \"preprod\", \"preprod\": { \"cluster\": \"http://preprod:8083\", \"username\": \"mickey\" }}");

            assertThat(new ConfigurationContext(tempDir).getCurrentContext().getUsername()).isEqualTo("mickey");
        }
    }

    @Nested
    class GetPassword {
        @Test
        void should_return_the_password_for_the_current_context() throws IOException {
            var configFile = tempDir.toPath().resolve(".kcctl");

            Files.writeString(configFile, "{ \"currentContext\": \"preprod\", \"preprod\": { \"cluster\": \"http://preprod:8083\", \"password\": \"p@ssword\" }}");

            assertThat(new ConfigurationContext(tempDir).getCurrentContext().getPassword()).isEqualTo("p@ssword");
        }
    }

    @Nested
    class GetclientConfig {
        @Test
        void should_return_a_map_of_the_client_config() throws IOException {
            var configFile = tempDir.toPath().resolve(".kcctl");

            Files.writeString(configFile,
                    "{ \"currentContext\": \"preprod\", \"preprod\": { \"cluster\": \"http://preprod:8083\", \"password\": \"p@ssword\", \"clientConfig\": { \"sasl.username\": \"myuser\", \"sasl.password\": \"mypassword\" } }}");
            assertThat(new ConfigurationContext(tempDir).getCurrentContext().getClientConfig())
                    .isEqualTo(new HashMap<String, Object>() {
                        {
                            this.put("sasl.password", "mypassword");
                            this.put("sasl.username", "myuser");
                        }
                    });
        }

        @Test
        void should_return_an_empty_map_of_the_client_config() throws IOException {
            var configFile = tempDir.toPath().resolve(".kcctl");

            Files.writeString(configFile,
                    "{ \"currentContext\": \"preprod\", \"preprod\": { \"cluster\": \"http://preprod:8083\", \"password\": \"p@ssword\", \"clientConfig\": { } }}");
            assertThat(new ConfigurationContext(tempDir).getCurrentContext().getClientConfig())
                    .isEqualTo(new HashMap<String, Object>());
        }

        @Test
        void should_return_a_null_map_of_the_client_config() throws IOException {
            var configFile = tempDir.toPath().resolve(".kcctl");

            Files.writeString(configFile,
                    "{ \"currentContext\": \"preprod\", \"preprod\": { \"cluster\": \"http://preprod:8083\", \"password\": \"p@ssword\", \"clientConfig\": null }}");
            assertThat(new ConfigurationContext(tempDir).getCurrentContext().getClientConfig())
                    .isNull();
        }

        @Test
        void should_return_a_null_map_of_the_client_config_when_missing() throws IOException {
            var configFile = tempDir.toPath().resolve(".kcctl");

            Files.writeString(configFile,
                    "{ \"currentContext\": \"preprod\", \"preprod\": { \"cluster\": \"http://preprod:8083\", \"password\": \"p@ssword\" }}");
            assertThat(new ConfigurationContext(tempDir).getCurrentContext().getClientConfig())
                    .isNull();
        }
    }

    static Stream<Arguments> setConfigurationArguments() {
        return Stream.of(
                arguments(
                        "local",
                        new Context(URI.create("http://localhost:8083"), "localhost:9092", "connect-offsets", null, null, null),
                        "{ 'currentContext': 'local', 'local': { 'cluster': 'http://localhost:8083', 'bootstrapServers': 'localhost:9092', 'offsetTopic': 'connect-offsets' }}"),
                arguments(
                        "local",
                        new Context(URI.create("http://localhost:8083"), null, null, null, null, null),
                        "{ 'currentContext': 'local', 'local': { 'cluster': 'http://localhost:8083' }}"));
    }
}
