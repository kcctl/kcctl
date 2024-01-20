/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.service;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConnectorOffset(
        @JsonProperty("partition") Map<String, ?> partition,
        @JsonProperty("offset") Map<String, ?> offset
) {
}
