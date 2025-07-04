package com.arangodb.tinkerpop.gremlin.persistence.simple;

import com.arangodb.tinkerpop.gremlin.persistence.ElementId;

public class SimpleId extends ElementId {

    public SimpleId(String prefix, String collection, String key) {
        super(prefix, collection, key);
    }

    @Override
    protected SimpleId withKey(String newKey) {
        ElementId.validateIdParts(newKey);
        return new SimpleId(prefix, collection, newKey);
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public String getId() {
        return getKey();
    }

}
