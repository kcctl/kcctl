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

@Command(name = "resume", subcommands = { ResumeConnectorCommand.class }, description = "Resumes connectors")
public class ResumeCommand {

    @CommandLine.Mixin
    HelpMixin help;

}
