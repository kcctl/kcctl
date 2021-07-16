package dev.morling.kccli.command;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import dev.morling.kccli.util.ConfigurationContext;
import picocli.CommandLine;

import static dev.morling.kccli.util.Colors.*;

@CommandLine.Command(name = "logger", description = "Changes the log level of given class/Connector path")
public class PatchLogLevelCommand implements Callable {

    @Inject
    ConfigurationContext context;

    @CommandLine.Parameters(paramLabel = "Logger NAME", description = "Name of the class/connector root path")
    String name;

    @CommandLine.Option(names = { "-l", "--level" }, description = "Name of LOGGER LEVEL to apply", required = true)
    String level;

    @Override
    public Object call() throws Exception {
        return null;
    }
}
