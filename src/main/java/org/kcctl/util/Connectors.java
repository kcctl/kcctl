/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.kcctl.service.KafkaConnectApi;

public class Connectors {
    private Connectors() {
    }

    public static Set<String> getSelectedConnectors(KafkaConnectApi kafkaConnectApi, Collection<String> names, boolean regexpMode) {
        if (regexpMode) {
            List<Pattern> namePatterns = names.stream().map(Pattern::compile).toList();
            return kafkaConnectApi.getConnectors().stream().filter(c -> namePatterns.stream().anyMatch(p -> p.matcher(c).matches()))
                    .collect(Collectors.toSet());
        }
        else {
            return new HashSet<>(names);
        }
    }
}
