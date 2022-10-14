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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.kcctl.service.ConfigInfos.ConfigKeyInfo;

public class Search {

    // Generic records... I'm going to hell for this
    public record SearchTerm<E>(Function<E, String> extractTerm, BiConsumer<E,String>setTerm)
    {

    public boolean matches(E element, Pattern regex) {
        String term = extractTerm.apply(element);
        Matcher matcher = regex.matcher(term);

        if (!matcher.find())
            return false;

        String highlightedTerm = matcher.replaceAll(matchResult -> {
            String match = matchResult.group();
            return match.isEmpty() ? "" : Colors.underline(matchResult.group());
        });
        setTerm.accept(element, highlightedTerm);
        return true;
    }}

    private static final SearchTerm<MutableConfigKeyInfo> CONFIG_NAME =
            new SearchTerm<>(MutableConfigKeyInfo::getName, MutableConfigKeyInfo::setName);
    private static final SearchTerm<MutableConfigKeyInfo> CONFIG_DESCRIPTION =
            new SearchTerm<>(MutableConfigKeyInfo::getDocumentation, MutableConfigKeyInfo::setDocumentation);

    /**
     * Find the {@link ConfigKeyInfo config properties} that match a given regular expression, using
     * all available terms.
     * @param config the configuration properties to search; may not be null, but may be empty
     * @param regex the regular expression to search for; may not be null
     * @return the configuration properties whose {@link ConfigKeyInfo#name() name}
     * or {@link ConfigKeyInfo#documentation() docstring} matches the regular expression. The order of the resulting
     * list will match the iteration order of the provided {@link Collection}
     */
    public static List<ConfigKeyInfo> searchConfig(Collection<ConfigKeyInfo> config, Pattern regex) {
        return searchConfig(config, regex, CONFIG_NAME, CONFIG_DESCRIPTION);
    }

    /**
     * Find the {@link ConfigKeyInfo config properties} whose {@link ConfigKeyInfo#name() names}
     * matches a given regular expression.
     * @param config the configuration properties to search; may not be null, but may be empty
     * @param regex the regular expression to search for; may not be null
     * @return the configuration properties whose {@link ConfigKeyInfo#name() name}
     * matches the regular expression. The order of the resulting list will match the iteration order
     * of the provided {@link Collection}
     */
    public static List<ConfigKeyInfo> searchConfigByName(Collection<ConfigKeyInfo> config, Pattern regex) {
        return searchConfig(config, regex, CONFIG_NAME);
    }

    /**
     * Find the {@link ConfigKeyInfo config properties} whose {@link ConfigKeyInfo#documentation() docstring}
     * matches a given regular expression.
     * @param config the configuration properties to search; may not be null, but may be empty
     * @param regex the regular expression to search for; may not be null
     * @return the configuration properties whose {@link ConfigKeyInfo#documentation() docstring}
     * matches the regular expression. The order of the resulting list will match the iteration order
     * of the provided {@link Collection}
     */
    public static List<ConfigKeyInfo> searchConfigByDescription(Collection<ConfigKeyInfo> config, Pattern regex) {
        return searchConfig(config, regex, CONFIG_DESCRIPTION);
    }

    @SafeVarargs
    private static List<ConfigKeyInfo> searchConfig(Collection<ConfigKeyInfo> config, Pattern regex, SearchTerm<MutableConfigKeyInfo>... terms) {
        List<MutableConfigKeyInfo> mutableConfig = config.stream().map(MutableConfigKeyInfo::fromRecord).toList();
        List<MutableConfigKeyInfo> filteredConfig = search(mutableConfig, regex, terms);
        return filteredConfig.stream().map(MutableConfigKeyInfo::toRecord).toList();
    }

    @SafeVarargs
    private static <E> List<E> search(Collection<E> elements, Pattern regex, SearchTerm<E>... terms) {
        List<E> result = new ArrayList<>();
        for (E element : elements) {
            boolean matched = false;
            for (SearchTerm<E> term : terms) {
                // Single-pipe OR operator forces evaluation of both operands, which we want in order
                // to continue to highlight matched terms even after a match has been established
                matched = matched | term.matches(element, regex);
            }
            if (matched)
                result.add(element);
        }
        return result;
    }

}
