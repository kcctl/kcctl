/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.service;

import jakarta.ws.rs.core.Response;

public class KafkaConnectException extends RuntimeException {

    public static KafkaConnectException from(Response response) {
        if (response.getStatusInfo() == Response.Status.UNAUTHORIZED) {
            return new KafkaConnectException(
                    response.readEntity(String.class),
                    Response.Status.UNAUTHORIZED.getStatusCode());
        }

        ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);

        if (response.getStatusInfo() == Response.Status.CONFLICT
                || errorResponse.message.contains("Request cannot be completed because a rebalance is expected")) {
            return new KafkaConnectConflictException(errorResponse.message);
        }
        else if (response.getStatusInfo() == Response.Status.NOT_FOUND) {
            return new KafkaConnectNotFoundException(errorResponse.message);
        }
        else {
            return new KafkaConnectException(errorResponse);
        }
    }

    private final int errorCode;

    public KafkaConnectException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public KafkaConnectException(ErrorResponse error) {
        super(getMessage(error.message));
        this.errorCode = error.error_code;
    }

    public int getErrorCode() {
        return errorCode;
    }

    private static String getMessage(String originalMessage) {
        String message = originalMessage.replaceAll("\\n", System.lineSeparator());
        message = message.replaceAll("\\nYou can also find the above list of errors at the endpoint `\\/connector-plugins\\/\\{connectorType\\}\\/config\\/validate`",
                "");
        return message;
    }
}
