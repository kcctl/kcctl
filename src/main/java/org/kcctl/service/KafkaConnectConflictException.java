/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.service;

import jakarta.ws.rs.core.Response;

public class KafkaConnectConflictException extends KafkaConnectException {

    public KafkaConnectConflictException(String message) {
        super(message, Response.Status.CONFLICT);
    }

}
