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
        final StringReader stringReader = new StringReader(formattedString);
        try {
            final Properties properties = new Properties();
            properties.load(stringReader);
            return properties;
        }
        finally {
            if (stringReader != null) {
                stringReader.close();
            }
        }
    }
}
