package com.arangodb.tinkerpop.gremlin.persistence.serde;

import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.arangodb.tinkerpop.gremlin.persistence.VertexData;
import com.arangodb.tinkerpop.gremlin.persistence.VertexPropertyData;
import com.arangodb.tinkerpop.gremlin.utils.Fields;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static com.arangodb.tinkerpop.gremlin.utils.Fields.*;

class VertexDataDeserializer extends JsonDeserializer<VertexData> {
    @Override
    public VertexData deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectCodec c = p.getCodec();
        ObjectNode root = c.readTree(p);
        ElementId id = c.treeToValue(root.get(ID), ElementId.class);
        String label = root.get(LABEL).asText();
        VertexData data = new VertexData(label, id);
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> meta = root.has(META)
                ? c.treeToValue(root.get(META), Map.class)
                : Collections.emptyMap();

        for (Map.Entry<String, JsonNode> prop : root.properties()) {
            if (!Fields.isReserved(prop.getKey())) {
                VertexPropertyData pd = new VertexPropertyData(c.treeToValue(prop.getValue(), Object.class));
                String key = prop.getKey();
                pd.putAll(meta.get(key));
                data.put(key, pd);
            }
        }

        return data;
    }
}
