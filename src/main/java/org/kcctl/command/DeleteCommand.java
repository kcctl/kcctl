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

import java.util.List;
import java.util.concurrent.Callable;

import org.kcctl.util.ConfigurationContext;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import javax.inject.Inject;

@Command(name = "delete", subcommands = { DeleteConnectorCommand.class }, description = "Deletes connectors")
public class DeleteCommand implements Callable<Integer> {

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
