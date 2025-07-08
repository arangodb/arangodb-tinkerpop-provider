package com.arangodb.tinkerpop.gremlin.persistence.serde;

import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.arangodb.tinkerpop.gremlin.persistence.ElementIdFactory;
import com.arangodb.tinkerpop.gremlin.persistence.VertexData;
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
        context.addSerializers(serializers);

        SimpleDeserializers deserializers = new SimpleDeserializers();
        deserializers.addDeserializer(ElementId.class, new ElementIdDeserializer(idFactory));
        deserializers.addDeserializer(VertexData.class, new VertexDataDeserializer());
        context.addDeserializers(deserializers);
    }
}
