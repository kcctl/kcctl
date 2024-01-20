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

@Command(name = "get", subcommands = { GetPluginsCommand.class, GetConnectorsCommand.class, GetOffsetsCommand.class,
        GetLoggersCommand.class,
        GetLoggerCommand.class }, description = "Displays information about connector plug-ins, connector offsets, created connectors, and loggers")
public class GetCommand {

    @CommandLine.Mixin
    HelpMixin help;

}
