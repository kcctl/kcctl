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
package org.kcctl.command;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.kcctl.completion.HelpCompletions;

import picocli.CommandLine;

/**
 * Adapted from the {@link CommandLine.HelpCommand} class. There are two primary differences:
 * <ul>
 *     <li>Information on nested subcommands is available (e.g., {@code kcctl help delete connectors})</li>
 *     <li>{@link  CommandLine#setAbbreviatedSubcommandsAllowed(boolean) Abbreviation of subcommands} is not supported (this feature is not currently used in kcctl)</li>
 * </ul>
 */
@CommandLine.Command(name = "help", header = {
        "Display help information about the specified command." }, synopsisHeading = "%nUsage: ", helpCommand = true, description = {
                "%nWhen no COMMAND is given, the usage help for the main command is displayed.", "If a COMMAND is specified, the help for that command is shown.%n" })
public class HelpCommand implements CommandLine.IHelpCommandInitializable2, Runnable {

    // Help for help!
    @CommandLine.Mixin
    HelpMixin help;
    @CommandLine.Parameters(paramLabel = "COMMAND", arity = "0..*", completionCandidates = HelpCompletions.class, description = {
            "The COMMAND to display the usage help message for." })
    private List<String> commands;
    private CommandLine self;
    private PrintWriter outWriter;
    private PrintWriter errWriter;
    private CommandLine.Help.ColorScheme colorScheme;

    public HelpCommand() {
    }

    @Override
    public void run() {
        CommandLine parent = this.self == null ? null : this.self.getParent();
        if (parent != null) {
            if (this.commands != null && !this.commands.isEmpty()) {
                CommandLine parentCommand = parent;
                CommandLine subCommand = null;
                List<String> fullCommandPath = new ArrayList<>();
                for (String command : commands) {
                    fullCommandPath.add(command);

                    subCommand = parentCommand
                            .getCommandSpec()
                            .subcommands()
                            .get(command);

                    if (subCommand == null) {
                        throw new CommandLine.ParameterException(
                                parent,
                                "Unknown command: '" + String.join(" ", fullCommandPath) + "'.",
                                null,
                                command);
                    }

                    parentCommand = subCommand;
                }

                subCommand.usage(this.outWriter, this.colorScheme);
            }
            else {
                parent.usage(this.outWriter, this.colorScheme);
            }

        }
    }

    @Override
    public void init(CommandLine helpCommandLine, CommandLine.Help.ColorScheme colorScheme, PrintWriter out, PrintWriter err) {
        this.self = helpCommandLine;
        this.colorScheme = colorScheme;
        this.outWriter = out;
        this.errWriter = err;
    }

}
