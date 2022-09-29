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
package org.kcctl.service;

import java.net.URI;
import java.util.Map;

import org.kcctl.util.Strings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Context(URI cluster, String bootstrapServers, String offsetTopic,
                      String username, String password,
                      Map<String, Object> clientConfig) {
    public Context(
            @JsonProperty("cluster") URI cluster,
            @JsonProperty("bootstrapServers") String bootstrapServers,
            @JsonProperty("offsetTopic") String offsetTopic,
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("clientConfig") Map<String, Object> clientConfig) {
        this.cluster = cluster;
        this.bootstrapServers = bootstrapServers;
        this.offsetTopic = offsetTopic;
        this.username = username;
        this.password = password;
        this.clientConfig = clientConfig;
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

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, Object> getClientConfig() {
        return this.clientConfig;
    }

    @JsonIgnore
    public boolean isUsingBasicAuthentication() {
        return !Strings.isBlank(this.getUsername()) &&
                !Strings.isBlank(this.getPassword());
    }

    public static Context defaultContext() {
        return new Context(URI.create("http://localhost:8083"), null, null, null, null, null);
    }
}
