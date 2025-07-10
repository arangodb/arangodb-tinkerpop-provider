package com.arangodb.tinkerpop.gremlin.persistence.serde;

import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.arangodb.tinkerpop.gremlin.persistence.ElementIdFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class ElementIdDeserializer extends JsonDeserializer<ElementId> {

    private final ElementIdFactory idFactory;

    public ElementIdDeserializer(ElementIdFactory idFactory) {
        this.idFactory = idFactory;
    }

    @Override
    public ElementId deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        return idFactory.parseId(p.getText());
    }
}
