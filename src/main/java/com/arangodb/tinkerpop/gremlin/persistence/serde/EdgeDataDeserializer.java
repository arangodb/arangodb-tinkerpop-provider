/*
 * Copyright 2025 ArangoDB GmbH and The University of York
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.tinkerpop.gremlin.persistence.serde;

import com.arangodb.tinkerpop.gremlin.persistence.EdgeData;
import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
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

    private final ArangoDBGraphConfig config;

    public EdgeDataDeserializer(ArangoDBGraphConfig config) {
        this.config = config;
    }

    @Override
    public EdgeData deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectCodec c = p.getCodec();
        ObjectNode root = c.readTree(p);
        ElementId id = c.treeToValue(root.get(ID), ElementId.class);
        String label;
        if (config.graphType == ArangoDBGraphConfig.GraphType.SIMPLE) {
            label = root.get(config.labelField).asText();
        } else {
            label = id.getLabel();
        }
        ElementId from = c.treeToValue(root.get(FROM), ElementId.class);
        ElementId to = c.treeToValue(root.get(TO), ElementId.class);
        EdgeData data = new EdgeData(label, id, from, to);

        for (Map.Entry<String, JsonNode> prop : root.properties()) {
            if (!config.isReservedField(prop.getKey())) {
                data.put(prop.getKey(), c.treeToValue(prop.getValue(), Object.class));
            }
        }

        return data;
    }
}
