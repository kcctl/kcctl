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
package org.kcctl.command;

import org.kcctl.service.Context;
import org.kcctl.util.ConfigurationContext;
import org.kcctl.util.Strings;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;
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
