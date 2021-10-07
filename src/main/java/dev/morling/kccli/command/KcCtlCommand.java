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
package dev.morling.kccli.command;

import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.config.ConfigProvider;

import dev.morling.kccli.service.ExecutionExceptionHandler;
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
        DeleteConnectorCommand.class,
        CommandLine.HelpCommand.class,
        ConnectorNamesCompletionCandidateCommand.class,
        TaskNamesCompletionCandidateCommand.class,
        LoggerNamesCompletionCandidateCommand.class
}, description = "A command-line interface for Kafka Connect"

)

public class KcCtlCommand {
    @Produces
    CommandLine getCommandLineInstance(PicocliCommandLineFactory factory) {
        return factory.create().setExecutionExceptionHandler(new ExecutionExceptionHandler());
    }
}

class VersionProviderWithConfigProvider implements IVersionProvider {
    public String[] getVersion() {
        String applicationName = ConfigProvider.getConfig().getValue("quarkus.application.name", String.class);
        String applicationVersion = ConfigProvider.getConfig().getValue("quarkus.application.version", String.class);
        return new String[]{ String.format("%s %s", applicationName, applicationVersion) };
    }
}
