package org.kcctl.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.ARRAY;

public class ConnectorNameExtractor {
    private final ObjectMapper objectMapper;

    public ConnectorNameExtractor() {
        this.objectMapper = JsonMapper.builder().build();
    }

    public String extract(String key) {
        try {
            return objectMapper.readValue(key, Key.class).connectorName;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonFormat(shape = ARRAY)
    @JsonPropertyOrder(alphabetic = true)
    record Key(@JsonProperty("connectorName") String connectorName) {
    }
}
