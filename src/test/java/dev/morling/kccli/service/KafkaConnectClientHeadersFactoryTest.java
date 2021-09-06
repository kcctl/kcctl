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
package dev.morling.kccli.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.morling.kccli.util.ConfigurationContext;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
class KafkaConnectClientHeadersFactoryTest {
    @TempDir
    File tempDir;

    @Nested
    class Update {
        @Test
        void should_inject_authorization_header_for_basic_auth() throws IOException {
            var configFile = tempDir.toPath().resolve(".kcctl");

            Files.writeString(configFile,
                    "{ \"currentContext\": \"local\", \"local\": { \"cluster\": \"http://localhost:8083\", \"username\": \"someuser\", \"password\": \"somepassword\" }}");

            var configurationContext = new ConfigurationContext(tempDir);
            var factory = new KafkaConnectClientHeadersFactory(configurationContext);

            var result = factory.update(null, null);

            assertThat(result.getFirst("Authorization")).isEqualTo("Basic c29tZXVzZXI6c29tZXBhc3N3b3Jk");
        }

        void should_return_empty_map_when_no_changes() throws IOException {
            var configFile = tempDir.toPath().resolve(".kcctl");

            Files.writeString(configFile, "{ \"currentContext\": \"local\", \"local\": { \"cluster\": \"http://localhost:8083\" }}");

            var configurationContext = new ConfigurationContext(tempDir);
            var factory = new KafkaConnectClientHeadersFactory(configurationContext);

            var result = factory.update(null, null);

            assertThat(result.isEmpty()).isEqualTo(true);
        }
    }
}
