/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.completion;

import java.util.Iterator;
import java.util.Map;

import org.kcctl.command.KcCtlCommand;

import picocli.CommandLine;

public class HelpCompletions implements Iterable<String> {

    @Override
    public Iterator<String> iterator() {
        CommandLine commandLine = new CommandLine(new KcCtlCommand());
        return commandLine.getSubcommands().entrySet().stream()
                .filter(e -> !e.getValue().getCommandSpec().usageMessage().hidden())
                .map(Map.Entry::getKey)
                .iterator();
    }

}
