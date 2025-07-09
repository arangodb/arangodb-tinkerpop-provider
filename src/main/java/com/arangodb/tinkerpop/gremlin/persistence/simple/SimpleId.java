package com.arangodb.tinkerpop.gremlin.persistence.simple;

import com.arangodb.tinkerpop.gremlin.persistence.ElementId;

public class SimpleId extends ElementId {

    public SimpleId(String prefix, String collection, String key) {
        super(prefix, collection, key);
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
