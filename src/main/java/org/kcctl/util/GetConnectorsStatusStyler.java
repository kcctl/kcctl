/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.util;

import java.util.List;
import java.util.stream.Collectors;

import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.Styler;

public class GetConnectorsStatusStyler implements Styler {

    @Override
    public List<String> styleCell(Column column, int row, int col, List<String> data) {
        return switch (column.getHeader()) {
            case " STATE" -> data.stream()
                    .map(value -> value.replace(value.trim(), Colors.colorizeState(value.trim())))
                    .collect(Collectors.toList());
            case " TASKS" -> data.stream()
                    .map(Colors::replaceColorState)
                    .collect(Collectors.toList());
            default -> data;
        };
    }
}
