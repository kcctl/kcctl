/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.completion;

import java.util.Iterator;
import java.util.List;

public class ConnectorNameCompletions implements Iterable<String> {

    @Override
    public Iterator<String> iterator() {
        return List.of("kcctl", "connector-name-completions").iterator();
    }

}
