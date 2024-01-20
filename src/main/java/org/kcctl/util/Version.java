/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.util;

public class Version {

    private final int major;
    private final int minor;

    /*
     * Valid Kafka versions are in the following formats:
     * - X.Y.Z
     * - X.Y.Z-SNAPSHOT
     */
    public Version(String version) {
        String[] parts = version.split("\\.");
        major = Integer.parseInt(parts[0]);
        minor = Integer.parseInt(parts[1]);
    }

    public Version(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public boolean greaterOrEquals(Version version) {
        return (major > version.major) ||
                (major == version.major && minor >= version.minor);
    }

    public String toString() {
        return String.format("%d.%d", major, minor);
    }
}
