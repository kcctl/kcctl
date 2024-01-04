/*
 *  Copyright 2024 The original authors
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.kcctl.IntegrationTest;
import org.kcctl.IntegrationTestProfile;
import org.kcctl.service.LoggerLevel;
import org.kcctl.support.InjectCommandContext;
import org.kcctl.support.KcctlCommandContext;
import org.kcctl.support.SkipIfConnectVersionIsOlderThan;

import io.debezium.testing.testcontainers.DebeziumContainer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SkipIfConnectVersionIsOlderThan("2.4")
class PatchLogLevelCommandTest extends IntegrationTest {

    private static final Duration CLUSTER_LOG_PATCH_TIMEOUT = Duration.ofSeconds(30);

    @InjectCommandContext
    KcctlCommandContext<PatchLogLevelCommand> context;

    @InjectCommandContext
    KcctlCommandContext<GetLoggerCommand> getContext;

    @Test
    public void should_patch_single_worker_with_no_scope() {
        testSingleWorker(null);
    }

    @Test
    public void should_patch_single_worker_with_worker_scope() {
        testSingleWorker("worker");
    }

    @Test
    public void should_patch_single_worker_with_unknown_scope() {
        testSingleWorker("unknown");
    }

    private void testSingleWorker(String scope) {
        String level = "WARN";
        runPatchCommand(level, scope, "root");
        Map<String, LoggerLevel> loggers = getContext.command().allLoggers();
        assertLogLevels(level, loggers);
        getContext.reset();

        level = "INFO";
        runPatchCommand(level, scope, "root");
        loggers = getContext.command().allLoggers();
        assertLogLevels(level, loggers);
        getContext.reset();
    }

    @Test
    @SkipIfConnectVersionIsOlderThan("3.7")
    public void should_patch_all_workers_with_cluster_scope() {
        try (DebeziumContainer secondWorker = secondWorker()) {
            KcctlCommandContext<GetLoggerCommand> getContextSecondWorker = prepareContext(GetLoggerCommand.class, secondWorker);
            String level = "WARN";
            runPatchCommand(level, "cluster", "root");
            assertAllLogLevels(level, getContext, getContextSecondWorker);

            level = "INFO";
            runPatchCommand(level, "cluster", "root");
            assertAllLogLevels(level, getContext, getContextSecondWorker);
        }
    }

    private void runPatchCommand(String level, String scope, String logger) {
        context.reset();
        List<String> args = new ArrayList<>();
        args.add("-l");
        args.add(level);
        if (scope != null) {
            args.add("-s");
            args.add(scope);
        }
        args.add(logger);
        context.runAndEnsureExitCodeOk(args.toArray(new String[0]));
    }

    private void assertLogLevels(String expectedLevel, Map<String, LoggerLevel> loggers) {
        assertTrue(loggers.size() >= 2);
        loggers.forEach((logger, loggerLevel) -> assertEquals(
                expectedLevel,
                loggerLevel.level().toUpperCase(Locale.ROOT),
                "invalid level for logger '" + logger + "'"));
    }

    @SafeVarargs
    private void assertAllLogLevels(String expectedLevel, KcctlCommandContext<GetLoggerCommand>... commands) {
        await()
                .atMost(CLUSTER_LOG_PATCH_TIMEOUT)
                .until(() -> {
                    for (KcctlCommandContext<GetLoggerCommand> commandContext : commands) {
                        Map<String, LoggerLevel> firstWorkerLoggers = commandContext.command().allLoggers();
                        try {
                            assertLogLevels(expectedLevel, firstWorkerLoggers);
                        }
                        finally {
                            commandContext.reset();
                        }
                    }
                    return true;
                });
    }

    private DebeziumContainer secondWorker() {
        DebeziumContainer result = debeziumContainer();
        result.start();
        return result;
    }

}
