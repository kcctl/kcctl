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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConfigurationContext {

    private static final String CONFIG_FILE = ".kcctl";
    private static final Path CONFIG_PATH = Path.of(System.getProperty("user.home"), CONFIG_FILE);

    public void setConfiguration(String cluster) {
        try {
            Files.writeString(CONFIG_PATH, "cluster=" + cluster, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e) {
            throw new RuntimeException("Couldn't write configuration file ~/" + CONFIG_FILE, e);
        }
    }

    public URI getCluster() {
        if (!Files.exists(CONFIG_PATH)) {
            return URI.create("http://localhost:8083");
        }

        try {
            String config = Files.readString(CONFIG_PATH);
            String[] parts = config.split("=");
            return URI.create(parts[1]);
        }
        catch (IOException e) {
            throw new RuntimeException("Couldn't read configuration file ~/" + CONFIG_FILE, e);
        }
    }
}
