/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
            // For Connector objects, STOPPED is identical to PAUSED
            // The only difference applies to tasks; with PAUSED, tasks stay up but
            // in an idling state; with STOPPED, all tasks are shut down
            case "STOPPED" -> ANSI_YELLOW + "STOPPED" + ANSI_RESET;
            case "FAILED" -> ANSI_RED + "FAILED" + ANSI_RESET;
            case "UNASSIGNED" -> ANSI_YELLOW + "UNASSIGNED" + ANSI_RESET;
            default -> state;
        };
    }

    public static String replaceColorState(String rawState) {
        return rawState.replace("RUNNING", colorizeState("RUNNING"))
                .replace("PAUSED", colorizeState("PAUSED"))
                .replace("STOPPED", colorizeState("STOPPED"))
                .replace("FAILED", colorizeState("FAILED"))
                .replace("UNASSIGNED", colorizeState("UNASSIGNED"));
    }
}
