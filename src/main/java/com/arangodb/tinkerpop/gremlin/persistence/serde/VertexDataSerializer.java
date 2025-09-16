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

import com.arangodb.tinkerpop.gremlin.persistence.VertexData;
import com.arangodb.tinkerpop.gremlin.persistence.VertexPropertyData;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import com.arangodb.tinkerpop.gremlin.utils.Fields;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class VertexDataSerializer extends JsonSerializer<VertexData> {

    private final ArangoDBGraphConfig config;

    VertexDataSerializer(ArangoDBGraphConfig config) {
        this.config = config;
    }

    @Override
    public void serialize(VertexData data, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        if (data.getKey() != null) {
            gen.writeStringField(Fields.KEY, data.getKey());
        }
        if (config.graphType == ArangoDBGraphConfig.GraphType.SIMPLE) {
            gen.writeStringField(config.labelField, data.getLabel());
        }

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
