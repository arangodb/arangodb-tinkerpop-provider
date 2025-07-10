package com.arangodb.tinkerpop.gremlin.persistence.serde;

import com.arangodb.tinkerpop.gremlin.persistence.VertexData;
import com.arangodb.tinkerpop.gremlin.persistence.VertexPropertyData;
import com.arangodb.tinkerpop.gremlin.utils.Fields;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.arangodb.tinkerpop.gremlin.utils.Fields.LABEL;

class VertexDataSerializer extends JsonSerializer<VertexData> {
    @Override
    public void serialize(VertexData data, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        if (data.getKey() != null) {
            gen.writeStringField(Fields.KEY, data.getKey());
        }
        gen.writeStringField(LABEL, data.getLabel());

        Map<String, Map<String, Object>> meta = new HashMap<>();

        for (Map.Entry<String, VertexPropertyData> entry : data.getProperties().entrySet()) {
            String key = entry.getKey();
            VertexPropertyData property = entry.getValue();
            gen.writeObjectField(key, property.getValue());
            if (!property.getProperties().isEmpty()) {
                meta.put(key, property.getProperties());
            }
        }

        gen.writeObjectFieldStart(Fields.META);
        for (Map.Entry<String, Map<String, Object>> entry : meta.entrySet()) {
            gen.writeObjectField(entry.getKey(), entry.getValue());
        }

        gen.writeEndObject();
        gen.writeEndObject();
    }
}
