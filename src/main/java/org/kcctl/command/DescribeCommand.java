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

@Command(name = "describe", subcommands = { DescribeConnectorCommand.class,
        DescribePluginCommand.class }, description = "Displays detailed information about the specified resources"

)
public class DescribeCommand {

    @CommandLine.Mixin
    HelpMixin help;

}
