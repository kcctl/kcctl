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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.kcctl.service.ConfigInfos.ConfigKeyInfo;

public class SearchTest {

    @Test
    public void testConfigSearch() {
        List<ConfigKeyInfo> config = Stream.of(
                new Prop("p1", "p1 docstring"),
                new Prop("p2", "p2 docstring"))
                .map(Prop::configKeyInfo)
                .toList();

        // We assert that we can match by name and by docstring,
        // and that we can highlight the portions of names that match the regex,
        // but not that we can highlight the portions of docstrings that match the regex.
        // We may want to add coverage in the future if this logic is refactored in order to
        // reduce the risk of regression.
        assertConfigSearchMatch(config, Pattern.compile("p1"), Colors.highlight("p1"));
        assertConfigSearchMatch(config, Pattern.compile("p2"), Colors.highlight("p2"));
        assertConfigSearchMatch(config, Pattern.compile("^p1$"), Colors.highlight("p1"));
        assertConfigSearchMatch(config, Pattern.compile("^p"),
                Colors.highlight("p") + "1", Colors.highlight("p") + "2");
        assertConfigSearchMatch(config, Pattern.compile("docstring"), "p1", "p2");
        assertConfigSearchMatch(config, Pattern.compile(".*"),
                Colors.highlight("p1"), Colors.highlight("p2"));
        assertConfigSearchMatch(config, Pattern.compile("^p$"));
        assertConfigSearchMatch(config, Pattern.compile("^docstring$"));
    }

    private record Prop(String name, String documentation) {
        public ConfigKeyInfo configKeyInfo() {
            return new ConfigKeyInfo(
                    name,
                    null,
                    false,
                    null,
                    null,
                    documentation,
                    null,
                    0,
                    null,
                    null,
                    null
            );
        }
    }

    private void assertConfigSearchMatch(List<ConfigKeyInfo> config, Pattern regex, String... expectedNames) {
        List<String> actualNames = Search.searchConfig(config, regex).stream()
                .map(ConfigKeyInfo::name)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(expectedNames), actualNames);
    }

}
