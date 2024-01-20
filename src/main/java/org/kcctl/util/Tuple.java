/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.util;

import java.util.List;

import static org.kcctl.util.Colors.ANSI_RESET;
import static org.kcctl.util.Colors.ANSI_WHITE_BOLD;

public record Tuple(String key, String value) {

    public String getValue() {
        return value;
    }

    public static void print(List<Tuple> tuples) {
        if (tuples.isEmpty()) {
            return;
        }

        int maxLength = tuples.stream()
                .mapToInt(t -> t.key.length() + 1)
                .max()
                .getAsInt();

        for (Tuple tuple : tuples) {
            if (!tuple.key.isEmpty()) {
                String key = tuple.key.replace("    Config", "    " + ANSI_WHITE_BOLD + "Config" + ANSI_RESET);
                System.out.printf("%-" + maxLength + "s  %s%n", key + ":", tuple.value);
            }
            else {
                System.out.println(tuple.value);
            }
        }
    }
}
