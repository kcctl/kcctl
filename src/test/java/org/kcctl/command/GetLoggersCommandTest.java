/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.command;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.kcctl.IntegrationTest;
import org.kcctl.IntegrationTestProfile;
import org.kcctl.support.InjectCommandContext;
import org.kcctl.support.KcctlCommandContext;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class GetLoggersCommandTest extends IntegrationTest {

    @InjectCommandContext
    KcctlCommandContext<GetLoggersCommand> context;

    @Test
    public void should_print_loggers() {
        context.runAndEnsureExitCodeOk();
        assertThat(context.output().toString().trim().lines())
                .map(String::trim)
                .containsExactly(
                        "LOGGER            LEVEL",
                        "org.reflections   \u001B[31mERROR\u001B[0m",
                        "root              \u001B[32mINFO\u001B[0m");
    }
}
