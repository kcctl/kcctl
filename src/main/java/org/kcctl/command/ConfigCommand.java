/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "config", subcommands = { SetContextCommand.class, GetContextsCommand.class,
        CurrentContextCommand.class, UseContextCommand.class, RemoveContextCommand.class }, description = "Sets or retrieves the configuration of this client"

)
public class ConfigCommand {

    @CommandLine.Mixin
    HelpMixin help;

}
