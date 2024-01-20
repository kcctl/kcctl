/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
                                @JsonProperty("default_value") String defaultValue,
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
