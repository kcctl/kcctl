package dev.morling.kccli.command;

import picocli.CommandLine;

@CommandLine.Command(name = "patch", subcommands = { PatchLogLevelCommand.class }, description = "Modify a configuration of the connector")
public class PatchCommand {
}
