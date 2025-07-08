package com.arangodb.tinkerpop.gremlin.persistence.serde;

import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.arangodb.tinkerpop.gremlin.persistence.VertexData;
import com.arangodb.tinkerpop.gremlin.persistence.VertexPropertyData;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.arangodb.tinkerpop.gremlin.utils.ReservedFields.LABEL;

class VertexDataDeserializer extends JsonDeserializer<VertexData> {
    @Override
    public VertexData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec c = p.getCodec();
        ObjectNode json = c.readTree(p);
        ElementId id = c.treeToValue(json.get("_id"), ElementId.class);
        String label = json.get(LABEL).asText();
        VertexData data = VertexData.of(label, id);

        //noinspection unchecked
        Map<String, Map<String, Object>> meta = Optional.ofNullable(json.get("_meta"))
                .map(metaNode -> {
                    try {
                        return c.treeToValue(metaNode, Map.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseGet(Collections::emptyMap);

        for (Map.Entry<String, JsonNode> prop : json.properties()) {
            VertexPropertyData pd = new VertexPropertyData(c.treeToValue(prop.getValue(), Object.class), meta.get(prop.getKey()));
            data.put(prop.getKey(), pd);
        }

        return data;
    }
}
