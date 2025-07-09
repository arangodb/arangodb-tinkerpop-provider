package com.arangodb.tinkerpop.gremlin.persistence;

import java.util.Objects;

public class VariablesData extends PropertiesContainer<Object> {

    private final String key;
    private String version;

    public VariablesData(String key, String version) {
        this.key = key;
        this.version = version;
    }

    public String getKey() {
        return key;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "VariablesData{" +
                "key='" + key + '\'' +
                ", version='" + version + '\'' +
                ", super=" + super.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VariablesData)) return false;
        if (!super.equals(o)) return false;
        VariablesData that = (VariablesData) o;
        return Objects.equals(key, that.key) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), key, version);
    }
}
