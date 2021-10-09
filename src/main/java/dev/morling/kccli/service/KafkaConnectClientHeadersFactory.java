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
package dev.morling.kccli.service;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import dev.morling.kccli.util.ConfigurationContext;

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
        MultivaluedMap<String, String> result = new MultivaluedHashMap<String, String>();
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
