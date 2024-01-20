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

@Command(name = "patch", subcommands = { PatchLogLevelCommand.class,
        PatchConnectorCommand.class, PatchOffsetsCommand.class }, description = "Modifies connector offsets, connector configurations, or logger levels")
public class PatchCommand {

    @CommandLine.Mixin
    HelpMixin help;

}
