/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class Strings {

    private Strings() {
    }

    public static boolean isBlank(String string) {
        if (string == null) {
            return true;
        }

        return string.trim().isEmpty();
    }

    public static Properties toProperties(String string) throws IOException {
        // Thanks to https://stackoverflow.com/a/58481108
        // for the pattern here
        final String formattedString = string.trim().replace(",", System.lineSeparator());
        try (StringReader stringReader = new StringReader(formattedString)) {
            final Properties properties = new Properties();
            properties.load(stringReader);
            return properties;
        }
    }
}
