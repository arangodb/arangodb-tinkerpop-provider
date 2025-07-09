package com.arangodb.tinkerpop.gremlin.persistence;

import java.util.Objects;

public abstract class ElementId {

    protected final String prefix;
    protected final String collection;
    protected String key;

    public static void validateIdParts(String... names) {
        for (String name : names) {
            if (name == null)
                continue;
            if (name.contains("_")) {
                throw new IllegalArgumentException(String.format("id part (%s) contains invalid character '_'", name));
            }
            if (name.contains("/")) {
                throw new IllegalArgumentException(String.format("id part (%s) contains invalid character '/'", name));
            }
        }
    }

    protected ElementId(String prefix, String collection, String key) {
        this.prefix = prefix;
        this.collection = collection;
        this.key = key;
    }

    public abstract String getLabel();

    public String getCollection() {
        return prefix + "_" + collection;
    }

    public abstract String getId();

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        validateIdParts(key);
        this.key = key;
    }

    public String toJson() {
        if (key == null) {
            return null;
        } else {
            return prefix + "_" + collection + "/" + key;
        }
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ElementId)) return false;
        ElementId elementId = (ElementId) o;
        return Objects.equals(prefix, elementId.prefix) && Objects.equals(collection, elementId.collection) && Objects.equals(key, elementId.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, collection, key);
    }

}
