/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.service;

import java.util.List;
import java.util.Map;

public record ConnectorInfo(String name, Map<String, String> config, List<TaskInfo> tasks, String type) {
}
