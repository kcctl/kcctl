/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import java.util.List;
import java.util.concurrent.Callable;

import jakarta.inject.Inject;

import org.kcctl.util.ConfigurationContext;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "delete", subcommands = { DeleteConnectorCommand.class, DeleteOffsetsCommand.class }, description = "Deletes connectors or their offsets")
public class DeleteCommand implements Callable<Integer> {

    @CommandLine.Mixin
    HelpMixin help;

    @CommandLine.Parameters
    // Hack: without this, picocli will fail to parse any commands that would otherwise get routed to
    // this class if arguments are provided, making it impossible to display a useful error message to users
    // who are used to using "delete" instead of "delete connector" to delete connectors
    // Luckily, even with this args field, picocli is smart enough to route commands like "delete connector foo"
    // to the DeleteConnectorCommand subcommand class, and send all other commands beginning with
    // "delete" to this one, so that we can display our helpful error message
    private List<String> args;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        throw new CommandLine.ParameterException(
                spec.commandLine(),
                "To delete a connector, use 'delete connector' instead of 'delete'");
    }

    @Inject
    public DeleteCommand(ConfigurationContext context) {
    }

    // Hack : Picocli currently require an empty constructor to generate the completion file
    public DeleteCommand() {
    }

}
