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

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(name = "kcctl", subcommands = {
        InfoCommand.class,
        ConfigCommand.class,
        GetCommand.class,
        DescribeCommand.class,
        ApplyCommand.class,
        PatchCommand.class,
        RestartCommand.class,
        PauseCommand.class,
        DeleteConnectorCommand.class,
        CommandLine.HelpCommand.class,
        ConnectorNamesCompletionCandidateCommand.class,
        TaskNamesCompletionCandidateCommand.class,
        LoggerNamesCompletionCandidateCommand.class
}, description = "A command-line interface for Kafka Connect"

)
public class KcCtlCommand {
}
