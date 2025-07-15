/*
 * Copyright 2025 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.tinkerpop.gremlin.utils;

import com.arangodb.tinkerpop.gremlin.persistence.EdgeData;
import com.arangodb.tinkerpop.gremlin.persistence.VertexData;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBEdge;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBVertex;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.arangodb.tinkerpop.gremlin.utils.Fields.LABEL;

public class AqlDeserializer {
    private final ArangoDBGraph graph;
    private final ObjectMapper mapper;

    public AqlDeserializer(ArangoDBGraph graph, ObjectMapper mapper) {
        this.graph = graph;
        this.mapper = mapper;
    }

    public Object deserialize(JsonNode node) {
        try {
            return doDeserialize(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Object doDeserialize(JsonNode node) throws IOException {
        if (isEdge(node)) {
            EdgeData data = mapper.readerFor(EdgeData.class).readValue(node);
            return new ArangoDBEdge(graph, data);
        } else if (isVertex(node)) {
            VertexData data = mapper.readerFor(VertexData.class).readValue(node);
            return new ArangoDBVertex(graph, data);
        } else if (node.isArray()) {
            ArrayList<Object> out = new ArrayList<>();
            for (JsonNode e : IteratorUtils.list(node.elements())) {
                out.add(deserialize(e));
            }
            return out;
        } else if (node.isObject()) {
            Map<String, Object> out = new HashMap<>();
            for (Map.Entry<String, JsonNode> f : node.properties()) {
                out.put(f.getKey(), deserialize(f.getValue()));
            }
            return out;
        } else {
            return mapper.readerFor(Object.class).readValue(node);
        }
    }

    private boolean isVertex(JsonNode node) {
        return node.has(Fields.KEY)
                && node.has(Fields.ID)
                && node.has(Fields.REV)
                && node.has(LABEL);
    }

    private boolean isEdge(JsonNode node) {
        return isVertex(node)
                && node.has(Fields.FROM)
                && node.has(Fields.TO);
    }
}
