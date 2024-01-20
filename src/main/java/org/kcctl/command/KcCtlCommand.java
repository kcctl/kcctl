/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.ConfigProvider;
import org.kcctl.service.ExecutionExceptionHandler;
import org.kcctl.util.ConfigurationContext;

import io.quarkus.picocli.runtime.PicocliCommandLineFactory;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;
import picocli.CommandLine.IVersionProvider;

@TopCommand()
@CommandLine.Command(name = "kcctl", mixinStandardHelpOptions = true, versionProvider = VersionProviderWithConfigProvider.class, subcommands = {
        InfoCommand.class,
        ConfigCommand.class,
        GetCommand.class,
        DescribeCommand.class,
        ApplyCommand.class,
        PatchCommand.class,
        RestartCommand.class,
        PauseCommand.class,
        ResumeCommand.class,
        StopCommand.class,
        DeleteCommand.class,
        HelpCommand.class,
        ConnectorNamesCompletionCandidateCommand.class,
        TaskNamesCompletionCandidateCommand.class,
        LoggerNamesCompletionCandidateCommand.class,
        ContextNamesCompletionCandidateCommand.class,
        PluginNamesCompletionCandidateCommand.class,
}, description = "A command-line interface for Kafka Connect"

)

public class KcCtlCommand {

    @Inject
    ConfigurationContext context;

    @Produces
    CommandLine getCommandLineInstance(PicocliCommandLineFactory factory) {
        return factory.create().setExecutionExceptionHandler(new ExecutionExceptionHandler(context.getCurrentContext()));
    }
}

class VersionProviderWithConfigProvider implements IVersionProvider {

    @Override
    public String[] getVersion() {
        String applicationName = ConfigProvider.getConfig().getValue("quarkus.application.name", String.class);
        String applicationVersion = ConfigProvider.getConfig().getValue("quarkus.application.version", String.class);
        return new String[]{ String.format("%s %s", applicationName, applicationVersion) };
    }
}
