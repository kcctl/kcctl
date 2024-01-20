/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import org.kcctl.completion.ContextNameCompletions;
import org.kcctl.util.ConfigurationContext;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "use-context", description = "Configures kcctl to use the specified context")
public class UseContextCommand implements Runnable {

    @CommandLine.Mixin
    HelpMixin help;

    @Parameters(index = "0", description = "Context name", completionCandidates = ContextNameCompletions.class)
    String contextName;

    @Override
    public void run() {
        ConfigurationContext context = new ConfigurationContext();

        if (context.getCurrentContextName().equals(contextName)) {
            System.out.println("Already using context " + contextName);
        }
        else {
            boolean success = context.setCurrentContext(contextName);

            if (success) {
                System.out.println("Using context '" + contextName + "'");
            }
            else {
                System.out.println("Couldn't change context; create a context named " + contextName + " first");
            }
        }
    }
}
