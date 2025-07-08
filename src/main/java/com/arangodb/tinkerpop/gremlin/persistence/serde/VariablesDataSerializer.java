package com.arangodb.tinkerpop.gremlin.persistence.serde;

import com.arangodb.tinkerpop.gremlin.persistence.VariablesData;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class VariablesDataSerializer extends JsonSerializer<VariablesData> {
    @Override
    public void serialize(VariablesData value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // TODO
    }
}
