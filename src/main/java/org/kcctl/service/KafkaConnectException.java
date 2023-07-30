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
package org.kcctl.service;

import javax.ws.rs.core.Response;

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
