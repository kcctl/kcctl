/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import org.kcctl.util.ConfigurationContext;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "remove-context", description = "Removes a context from your contexts")
public class RemoveContextCommand implements Callable<Integer> {

    @CommandLine.Mixin
    HelpMixin help;

    @Parameters(index = "0", description = "Context name")
    String contextName;

    @Override
    public Integer call() {
        ConfigurationContext context = new ConfigurationContext();

        if (context.getCurrentContextName().equals(contextName)) {
            System.out.println("Set another context before removing the current context.");
            return 1;
        }
        else {
            boolean success = context.removeContext(contextName);

            if (success) {
                System.out.println("Removed context '" + contextName + "'");
            }
            else {
                System.out.println("Couldn't remove context; There was no context named '" + contextName + "'");
                return 1;
            }
        }

        return 0;
    }

}
