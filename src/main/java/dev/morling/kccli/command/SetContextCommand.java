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

import dev.morling.kccli.util.ConfigurationContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "set-context")
public class SetContextCommand implements Runnable {

    @Option(names = { "--cluster" }, description = "URL of the Kafka Connect cluster to connect to", required = true)
    String cluster;

    @Override
    public void run() {
        ConfigurationContext context = new ConfigurationContext();
        context.setConfiguration(cluster);
        System.out.println("Successfully set context to " + cluster);
    }
}
