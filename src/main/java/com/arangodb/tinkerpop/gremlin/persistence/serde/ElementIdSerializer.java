package com.arangodb.tinkerpop.gremlin.persistence.serde;

import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ElementIdSerializer extends JsonSerializer<ElementId> {
    @Override
    public void serialize(ElementId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String json = value.toJson();
        if (json == null) {
            gen.writeNull();
        } else {
            gen.writeString(json);
        }
    }
}
