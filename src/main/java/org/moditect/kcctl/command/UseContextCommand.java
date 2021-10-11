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
package org.moditect.kcctl.command;

import org.moditect.kcctl.completion.ContextNameCompletions;
import org.moditect.kcctl.util.ConfigurationContext;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "use-context", description = "Configures kcctl to use the specified context")
public class UseContextCommand implements Runnable {

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
                System.out.println("Using context " + contextName);
            }
            else {
                System.out.println("Couldn't change context; create a context named " + contextName + " first");
            }
        }
    }
}
