/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

// Work-around for https://github.com/quarkusio/quarkus/issues/18352:
// Dev mode only works if there's at least one test present
@QuarkusTest
public class DummyTest {

    @Test
    public void dummyTest() {
    }
}
