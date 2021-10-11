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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersionTest {

    @Test
    public void testVersion() {
        Version version = new Version(2, 8);
        assertEquals("2.8", version.toString());

        assertTrue(version.greaterOrEquals(new Version("1.0.0")));
        assertTrue(version.greaterOrEquals(new Version("2.0.1")));
        assertTrue(version.greaterOrEquals(new Version("2.1.0-SNAPSHOT")));
        assertTrue(version.greaterOrEquals(new Version("2.8.0")));
        assertTrue(version.greaterOrEquals(new Version("2.8.1")));

        assertFalse(version.greaterOrEquals(new Version("2.9.0")));
        assertFalse(version.greaterOrEquals(new Version("3.0.0")));
        assertFalse(version.greaterOrEquals(new Version("3.7.0")));
    }
}
