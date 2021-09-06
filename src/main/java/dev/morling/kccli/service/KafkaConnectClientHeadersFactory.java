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

        if (context.getContext().isUsingBasicAuthentication()) {
            result.add("Authorization",
                    generateBasicAuthHeaderValue(
                            context.getContext().getUsername(),
                            context.getContext().getPassword()));
            return result;
        }

        return result;
    }

    private static String generateBasicAuthHeaderValue(String username, String password) {
        return String.format("Basic %s",
                new Base64().encodeAsString(String.format("%s:%s", username, password).getBytes()));
    }
}
