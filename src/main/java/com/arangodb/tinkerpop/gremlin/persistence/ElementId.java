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
