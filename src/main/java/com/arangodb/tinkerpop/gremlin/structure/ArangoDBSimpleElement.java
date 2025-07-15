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

package com.arangodb.tinkerpop.gremlin.structure;

import com.arangodb.tinkerpop.gremlin.persistence.PropertiesContainer;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil;
import org.apache.tinkerpop.gremlin.structure.Property;

public abstract class ArangoDBSimpleElement<D extends PropertiesContainer<Object>> extends ArangoDBElement<Object, D> {
    ArangoDBSimpleElement(ArangoDBGraph graph, D data) {
        super(graph, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <V> Property<V> createProperty(String key, Object value) {
        return new ArangoDBProperty<>(this, key, (V) value);
    }

    @Override
    public <V> Property<V> property(String key, V value) {
        if (removed()) throw Exceptions.elementAlreadyRemoved(id());
        ArangoDBUtil.validateProperty(key, value);
        data().put(key, value);
        doUpdate();
        return createProperty(key, value);
    }

    void removeProperty(String key) {
        if (removed()) throw Exceptions.elementAlreadyRemoved(id());
        data.remove(key);
        doUpdate();
    }

}
