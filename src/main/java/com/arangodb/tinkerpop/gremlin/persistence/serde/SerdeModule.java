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

import com.arangodb.tinkerpop.gremlin.persistence.*;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;

public class SerdeModule extends SimpleModule {
    private final ElementIdFactory idFactory;

    public SerdeModule(ElementIdFactory idFactory) {
        this.idFactory = idFactory;
    }

    @Override
    public void setupModule(SetupContext context) {
        SimpleSerializers serializers = new SimpleSerializers();
        serializers.addSerializer(ElementId.class, new ElementIdSerializer());
        serializers.addSerializer(VertexData.class, new VertexDataSerializer());
        serializers.addSerializer(EdgeData.class, new EdgeDataSerializer());
        serializers.addSerializer(VariablesData.class, new VariablesDataSerializer());
        context.addSerializers(serializers);

        SimpleDeserializers deserializers = new SimpleDeserializers();
        deserializers.addDeserializer(ElementId.class, new ElementIdDeserializer(idFactory));
        deserializers.addDeserializer(VertexData.class, new VertexDataDeserializer());
        deserializers.addDeserializer(EdgeData.class, new EdgeDataDeserializer());
        deserializers.addDeserializer(VariablesData.class, new VariablesDataDeserializer());
        context.addDeserializers(deserializers);
    }
}
