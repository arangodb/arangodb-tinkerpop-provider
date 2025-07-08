package com.arangodb.tinkerpop.gremlin.persistence.serde;

import com.arangodb.tinkerpop.gremlin.persistence.VertexData;
import com.arangodb.tinkerpop.gremlin.persistence.VertexPropertyData;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.arangodb.tinkerpop.gremlin.utils.ReservedFields.LABEL;

class VertexDataSerializer extends JsonSerializer<VertexData> {
    @Override
    public void serialize(VertexData value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        if (value.id() != null) {
            gen.writeObjectField("_id", value.id());
        }
        if (value.getKey() != null) {
            gen.writeStringField("_key", value.getKey());
        }
        gen.writeStringField(LABEL, value.getLabel());

        Map<String, Map<String, Object>> metaProperties = new HashMap<>();

        if (!value.getProperties().isEmpty()) {
            for (Map.Entry<String, VertexPropertyData> entry : value.getProperties().entrySet()) {
                String key = entry.getKey();
                VertexPropertyData property = entry.getValue();
                gen.writeObjectField(key, property.getValue());
                if (!property.getProperties().isEmpty()) {
                    metaProperties.put(key, property.getProperties());
                }
            }
        }

        if (!metaProperties.isEmpty()) {
            gen.writeObjectFieldStart("_meta");
            for (Map.Entry<String, Map<String, Object>> entry : metaProperties.entrySet()) {
                gen.writeObjectField(entry.getKey(), entry.getValue());
            }
            gen.writeEndObject();
        }

        gen.writeEndObject();
    }
}
