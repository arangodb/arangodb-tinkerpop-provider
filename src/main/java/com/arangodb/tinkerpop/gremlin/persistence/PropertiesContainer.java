/*
 * Copyright 2025 ArangoDB GmbH, Cologne, Germany
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

import java.util.*;


public abstract class PropertiesContainer<V> {

    private final Map<String, V> properties = new HashMap<>();

    public Map<String, V> getProperties() {
        return properties;
    }

    public Set<String> keySet() {
        return properties.keySet();
    }

    public Collection<V> values() {
        return properties.values();
    }

    public V get(String key) {
        return properties.get(key);
    }

    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    public void put(String key, V value) {
        properties.put(key, value);
    }

    public void putAll(Map<String, V> map) {
        if (map != null) {
            properties.putAll(map);
        }
    }

    public void remove(String key) {
        properties.remove(key);
    }

    @Override
    public String toString() {
        return "PropertiesContainer{" +
                "properties=" + properties +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PropertiesContainer)) return false;
        PropertiesContainer<?> that = (PropertiesContainer<?>) o;
        return Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(properties);
    }
}
