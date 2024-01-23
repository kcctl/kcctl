/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.service;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.kcctl.util.ConfigurationContext;

public class KafkaConnectClientHeadersFactory implements ClientHeadersFactory {

    @Inject
    ConfigurationContext context;

    public KafkaConnectClientHeadersFactory(ConfigurationContext context) {
        super();

        this.context = context;
    }

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
                                                 MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        Context currentContext = context.getCurrentContext();

        if (currentContext.isUsingBasicAuthentication()) {
            result.add("Authorization",
                    generateBasicAuthHeaderValue(
                            currentContext.getUsername(),
                            currentContext.getPassword()));
            return result;
        }

        return result;
    }

    private static String generateBasicAuthHeaderValue(String username, String password) {
        return String.format("Basic %s",
                new Base64().encodeAsString(String.format("%s:%s", username, password).getBytes()));
    }
}
