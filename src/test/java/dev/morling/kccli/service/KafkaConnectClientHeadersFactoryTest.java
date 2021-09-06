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
