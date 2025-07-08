package com.arangodb.tinkerpop.gremlin.persistence.serde;

import com.arangodb.tinkerpop.gremlin.persistence.EdgeData;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class EdgeDataSerializer extends JsonSerializer<EdgeData> {
    @Override
    public void serialize(EdgeData value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // TODO
    }
}
