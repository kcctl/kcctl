/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.util;

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
