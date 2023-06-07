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
