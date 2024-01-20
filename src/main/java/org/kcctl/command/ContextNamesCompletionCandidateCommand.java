/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import java.util.Set;

import jakarta.inject.Inject;

import org.kcctl.util.ConfigurationContext;

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
