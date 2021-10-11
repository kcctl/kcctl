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
package org.moditect.kcctl.util;

import java.util.List;

import static org.moditect.kcctl.util.Colors.ANSI_RESET;
import static org.moditect.kcctl.util.Colors.ANSI_WHITE_BOLD;

public class Tuple {

    private final String key;
    private final String value;

    public Tuple(String key, String value) {
        this.key = key;
        this.value = value;
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
