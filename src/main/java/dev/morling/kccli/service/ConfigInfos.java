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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigInfos {

    public String name;
    @JsonProperty("error_count")
    public int errorCount;
    public List<String> groups;
    public List<ConfigInfo> configs;

    public static class ConfigInfo {
        @JsonProperty("definition")
        public ConfigKeyInfo configKey;
        @JsonProperty("value")
        public ConfigValueInfo configValue;
    }

    public static class ConfigKeyInfo {
        public String name;
        public String type;
        public boolean required;
        public String defaultValue;
        public String importance;
        public String documentation;
        public String group;
        public int orderInGroup;
        public String width;
        public String displayName;
        public List<String> dependents;
    }

    public static class ConfigValueInfo {
        public String name;
        public String value;
        public List<String> recommendedValues;
        public List<String> errors;
        public boolean visible;
    }

}
