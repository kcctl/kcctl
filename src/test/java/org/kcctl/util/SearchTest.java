/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.util;

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
        assertEquals(List.of(expectedNames), actualNames);
    }

}
