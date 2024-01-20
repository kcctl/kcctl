/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import jakarta.inject.Inject;

import org.kcctl.service.Context;
import org.kcctl.util.ConfigurationContext;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "current-context", description = "Displays the current context")
public class CurrentContextCommand implements Runnable {

    @CommandLine.Mixin
    HelpMixin help;

    @Inject
    ConfigurationContext context;

    @Override
    public void run() {
        Context currentContext = context.getCurrentContext();
        String[][] data = new String[5][];
        data[0] = new String[]{ "Kafka Connect URI", currentContext.getCluster().toASCIIString() };
        data[1] = new String[]{ "User", currentContext.getUsername() };
        data[2] = new String[]{ "Password", currentContext.getPassword() != null ? "***" : "" };
        data[3] = new String[]{ "Bootstrap servers", currentContext.getBootstrapServers() };
        data[4] = new String[]{ "Offset topic", currentContext.getOffsetTopic() };

        String table = AsciiTable.getTable(AsciiTable.NO_BORDERS,
                new Column[]{
                        new Column().header("KEY").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("VALUE").dataAlign(HorizontalAlign.LEFT)
                },
                data);

        System.out.println("Using context '" + context.getCurrentContextName() + "'");
        System.out.println();
        System.out.println(table);
    }
}
