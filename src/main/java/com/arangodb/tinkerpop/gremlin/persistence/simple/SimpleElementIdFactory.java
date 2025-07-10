package com.arangodb.tinkerpop.gremlin.persistence.simple;

import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.arangodb.tinkerpop.gremlin.persistence.ElementIdFactory;


public class SimpleElementIdFactory extends ElementIdFactory {

    public SimpleElementIdFactory(String prefix) {
        super(prefix);
    }

    @Override
    protected String inferCollection(final String collection, final String label, final String defaultLabel) {
        return defaultLabel;
    }

    @Override
    protected void validateId(String id) {
        if (id.contains("_")) {
            throw new IllegalArgumentException(String.format("id (%s) contains invalid character '_'", id));
        }
        if (id.contains("/")) {
            throw new IllegalArgumentException(String.format("id (%s) contains invalid character '/'", id));
        }
    }

    @Override
    protected ElementId doCreate(String prefix, String collection, String key) {
        return new SimpleId(prefix, collection, key);
    }
}
