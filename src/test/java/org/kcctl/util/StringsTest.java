/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.util;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringsTest {

    @Nested
    public class IsBlank {
        @Test
        public void nullIsBlank() {
            assertTrue(Strings.isBlank(null));
        }

        @Test
        public void emptyIsBlank() {
            assertTrue(Strings.isBlank(""));
        }

        @Test
        public void whitespaceIsBlank() {
            assertTrue(Strings.isBlank(" "));
        }

        @Test
        public void bobIsNotBlank() {
            assertFalse(Strings.isBlank("bob"));
        }

        @Test
        public void bobWithWhitespaceIsNotBlank() {
            assertFalse(Strings.isBlank("  bob  "));
        }
    }

    @Nested
    public class ToProperties {
        @Test
        public void singlePropertyLoadProperly() throws IOException {
            final Properties props = Strings.toProperties("this.is.a.property=true");
            assertEquals(props.getProperty("this.is.a.property"), "true");
        }

        @Test
        public void multiplePropertiesLoadProperly() throws IOException {
            final Properties props = Strings.toProperties("this.is.a.property=true,this.is.another.one=hello");

            assertEquals("true", props.getProperty("this.is.a.property"));
            assertEquals("hello", props.getProperty("this.is.another.one"));
        }

        @Test
        public void whitespaceAroundPropertiesLoadProperly() throws IOException {
            final Properties props = Strings.toProperties("      this.is.a.property=true, this.is.another.one=hello   ");

            assertEquals("true", props.getProperty("this.is.a.property"));
            assertEquals("hello", props.getProperty("this.is.another.one"));
        }

        @Test
        public void noPropertiesInStringIsEmpty() throws IOException {
            final Properties props = Strings.toProperties("");

            assertTrue(props.isEmpty());
        }

        @Test
        public void commaOnlyStringIsEmpty() throws IOException {
            final Properties props = Strings.toProperties(",");

            assertTrue(props.isEmpty());
        }

        @Test
        public void multipleCommasOnlyStringIsEmpty() throws IOException {
            final Properties props = Strings.toProperties(",,");

            assertTrue(props.isEmpty());
        }
    }
}
