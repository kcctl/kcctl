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

public class Colors {

    // ANSI escape sequence
    private static final String E = "\u001B[";

    public static final String ANSI_RESET = E + "0m";
    public static final String ANSI_BLACK = E + "30m";
    public static final String ANSI_RED = E + "31m";
    public static final String ANSI_GREEN = E + "32m";
    public static final String ANSI_YELLOW = E + "33m";
    public static final String ANSI_BLUE = E + "34m";
    public static final String ANSI_PURPLE = E + "35m";
    public static final String ANSI_CYAN = E + "36m";
    public static final String ANSI_WHITE = E + "37m";
    public static final String ANSI_WHITE_BOLD = E + "37;1m";
    public static final String ANSI_BOLD = E + "1m";
    public static final String ANSI_WHITE_BACKGROUND = E + "107m";

    /**
     * Highlight some text with bold, black font and a white background
     * @param input the text to highlight; if null, the literal "null" is used in its place
     * @return the highlighted text; never null
     */
    public static String highlight(String input) {
        return ANSI_WHITE_BACKGROUND + ANSI_BLACK + ANSI_BOLD + input + ANSI_RESET;
    }

    public static String colorizeState(String state) {
        return switch (state) {
            case "RUNNING" -> ANSI_GREEN + "RUNNING" + ANSI_RESET;
            case "PAUSED" -> ANSI_YELLOW + "PAUSED" + ANSI_RESET;
            case "FAILED" -> ANSI_RED + "FAILED" + ANSI_RESET;
            case "UNASSIGNED" -> ANSI_YELLOW + "UNASSIGNED" + ANSI_RESET;
            default -> state;
        };
    }

    public static String replaceColorState(String rawState) {
        return rawState.replace("RUNNING", colorizeState("RUNNING"))
                .replace("PAUSED", colorizeState("PAUSED"))
                .replace("FAILED", colorizeState("FAILED"))
                .replace("UNASSIGNED", colorizeState("UNASSIGNED"));
    }
}
