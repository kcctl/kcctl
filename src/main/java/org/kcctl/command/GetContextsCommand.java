/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import java.util.Map;

import jakarta.inject.Inject;

import org.kcctl.service.Context;
import org.kcctl.util.ConfigurationContext;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "get-contexts", description = "Get all contexts")
public class GetContextsCommand implements Runnable {

    @CommandLine.Mixin
    HelpMixin help;

    @Inject
    ConfigurationContext configContext;

    @Override
    public void run() {
        Map<String, Context> contexts = configContext.getContexts();
        String current = configContext.getCurrentContextName();

        String[][] data = contexts.entrySet()
                .stream()
                .map(e -> new String[]{ e.getKey() + (e.getKey().equals(current) ? "*" : ""), e.getValue().getCluster().toASCIIString() })
                .toArray(size -> new String[size][]);

        String table = AsciiTable.getTable(AsciiTable.NO_BORDERS,
                new Column[]{
                        new Column().header("NAME").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("KAFKA CONNECT URI").dataAlign(HorizontalAlign.LEFT)
                },
                data);

        System.out.println(table);
    }
}
