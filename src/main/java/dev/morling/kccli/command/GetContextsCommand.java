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

import java.util.Map;

import javax.inject.Inject;

import dev.morling.kccli.service.Context;
import dev.morling.kccli.util.ConfigurationContext;
import picocli.CommandLine.Command;

@Command(name = "get-contexts", description = "Get all contexts")
public class GetContextsCommand implements Runnable {

    @Inject
    ConfigurationContext configContext;

    @Override
    public void run() {
        Map<String, Context> contexts = configContext.getContexts();
        contexts.forEach((name, context) -> {
            String clusterUri = context.getCluster().toASCIIString();
            System.out.println(name + " is set to " + clusterUri);
        });
    }
}
