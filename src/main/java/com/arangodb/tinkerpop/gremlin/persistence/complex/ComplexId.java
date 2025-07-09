package com.arangodb.tinkerpop.gremlin.persistence.complex;

import com.arangodb.tinkerpop.gremlin.persistence.ElementId;

public class ComplexId extends ElementId {

    public ComplexId(String prefix, String collection, String key) {
        super(prefix, collection, key);
    }

    @Override
    public String getLabel() {
        return collection;
    }

    @Override
    public String getId() {
        return collection + "/" + key;
    }

}
