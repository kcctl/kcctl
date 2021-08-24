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

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Context {
    private final URI cluster;
    private final String bootstrapServers;
    private final String offsetTopic;

    public Context(
                   @JsonProperty("cluster") URI cluster,
                   @JsonProperty("bootstrapServers") String bootstrapServers,
                   @JsonProperty("offsetTopic") String offsetTopic) {
        this.cluster = cluster;
        this.bootstrapServers = bootstrapServers;
        this.offsetTopic = offsetTopic;
    }

    public URI getCluster() {
        return cluster;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getOffsetTopic() {
        return offsetTopic;
    }

    public static Context defaultContext() {
        return new Context(URI.create("http://localhost:8083"), null, null);
    }
}
