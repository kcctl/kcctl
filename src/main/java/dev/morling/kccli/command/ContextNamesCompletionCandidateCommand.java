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

import java.util.Set;

import javax.inject.Inject;

import dev.morling.kccli.util.ConfigurationContext;
import picocli.CommandLine.Command;

@Command(name = "context-name-completions", hidden = true)
public class ContextNamesCompletionCandidateCommand implements Runnable {

    @Inject
    ConfigurationContext context;

    @Override
    public void run() {
        Set<String> contexts = context.getContexts().keySet();
        System.out.println(String.join(" ", contexts));
    }
}
