package com.arangodb.tinkerpop.gremlin.persistence.serde;

import com.arangodb.tinkerpop.gremlin.persistence.EdgeData;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class EdgeDataDeserializer extends JsonDeserializer<EdgeData> {
    @Override
    public EdgeData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        // TODO
        return null;
    }
}
