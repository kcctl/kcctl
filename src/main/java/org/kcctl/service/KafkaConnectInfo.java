/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.service;

public record KafkaConnectInfo(String version, String commit, String kafka_cluster_id) {

    @Override
    public String toString() {
        return "KafkaConnectInfo [version=" + version + ", commit=" + commit + ", kafka_cluster_id=" + kafka_cluster_id
                + "]";
    }
}
