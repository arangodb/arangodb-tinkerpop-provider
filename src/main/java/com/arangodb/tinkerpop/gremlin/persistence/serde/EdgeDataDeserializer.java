package com.arangodb.tinkerpop.gremlin.persistence.serde;

import com.arangodb.tinkerpop.gremlin.persistence.EdgeData;
import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.arangodb.tinkerpop.gremlin.utils.Fields;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Map;

import static com.arangodb.tinkerpop.gremlin.utils.Fields.*;

public class EdgeDataDeserializer extends JsonDeserializer<EdgeData> {
    @Override
    public EdgeData deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectCodec c = p.getCodec();
        ObjectNode root = c.readTree(p);
        ElementId id = c.treeToValue(root.get(ID), ElementId.class);
        String label = root.get(LABEL).asText();
        ElementId from = c.treeToValue(root.get(FROM), ElementId.class);
        ElementId to = c.treeToValue(root.get(TO), ElementId.class);
        EdgeData data = new EdgeData(label, id, from, to);

        for (Map.Entry<String, JsonNode> prop : root.properties()) {
            if (!Fields.isReserved(prop.getKey())) {
                data.put(prop.getKey(), c.treeToValue(prop.getValue(), Object.class));
            }
        }

        return data;
    }
}
