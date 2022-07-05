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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConfigInfos(String name,
                          @JsonProperty("error_count") int errorCount,
                          List<String> groups,
                          List<ConfigInfo> configs) {

    public record ConfigInfo(@JsonProperty("definition") ConfigKeyInfo configKey,
                             @JsonProperty("value") ConfigValueInfo configValue) {
    }

    public record ConfigKeyInfo(String name,
                                String type,
                                boolean required,
                                String defaultValue,
                                String importance,
                                String documentation,
                                String group,
                                int orderInGroup,
                                String width,
                                String displayName,
                                List<String> dependents) {
    }

    public record ConfigValueInfo(String name,
                                  String value,
                                  List<String> recommendedValues,
                                  List<String> errors,
                                  boolean visible) {
    }

}
