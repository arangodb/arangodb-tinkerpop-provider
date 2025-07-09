package com.arangodb.tinkerpop.gremlin.persistence.serde;

import com.arangodb.tinkerpop.gremlin.persistence.VariablesData;
import com.arangodb.tinkerpop.gremlin.utils.Fields;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

import static com.arangodb.tinkerpop.gremlin.utils.Fields.*;

public class VariablesDataSerializer extends JsonSerializer<VariablesData> {
    @Override
    public void serialize(VariablesData data, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        if (data.getKey() != null) {
            gen.writeStringField(Fields.KEY, data.getKey());
        }
        gen.writeStringField(VERSION, data.getVersion());

        for (Map.Entry<String, Object> entry : data.getProperties().entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();
            gen.writeObjectField(k, v);
        }
        gen.writeEndObject();
    }
}
