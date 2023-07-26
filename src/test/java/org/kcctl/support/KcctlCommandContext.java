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
package org.kcctl.support;

import java.io.StringWriter;

import picocli.CommandLine;

public record KcctlCommandContext<T>(T command, CommandLine commandLine, StringWriter output, StringWriter error) {

    /**
     * Clear the {@link #output() stdout} and {@link #error() stderr} for this command.
     * Useful if multiple invocations of the command take place, and only the output of
     * the most recent invocation is desired each time.
     */
    public void reset() {
        output.getBuffer().setLength(0);
        error.getBuffer().setLength(0);
    }
}
