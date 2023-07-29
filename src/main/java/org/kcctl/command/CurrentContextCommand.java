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

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import jakarta.inject.Inject;
import picocli.CommandLine.Command;

@Command(name = "current-context", description = "Displays the current context")
public class CurrentContextCommand implements Runnable {

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
