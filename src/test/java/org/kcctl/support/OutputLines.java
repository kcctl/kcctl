/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.support;

import java.util.List;

/**
 * The lines of a command's table output with each run of whitespace collapsed into one space, so
 * that assertions do not depend on column widths, which change with the longest entry of a table.
 */
public final class OutputLines {

    private OutputLines() {
    }

    public static List<String> of(String output) {
        return output.trim().lines()
                .map(line -> line.trim().replaceAll("\\s+", " "))
                .toList();
    }
}
