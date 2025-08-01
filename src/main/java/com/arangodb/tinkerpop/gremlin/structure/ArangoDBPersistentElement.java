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

package com.arangodb.tinkerpop.gremlin.structure;

import com.arangodb.entity.DocumentEntity;
import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.arangodb.tinkerpop.gremlin.persistence.PersistentData;
import org.apache.tinkerpop.gremlin.structure.Element;

public interface ArangoDBPersistentElement extends Element {

    @Override
    ArangoDBGraph graph();

    PersistentData data();

    default String key() {
        return data().getKey();
    }

    default void update(DocumentEntity entity) {
        data().update(entity);
    }

    @Override
    default String label() {
        return data().getLabel();
    }

    default String collection() {
        return data().collection();
    }

    default ElementId elementId() {
        return data().elementId();
    }

    @Override
    default String id() {
        return data().id();
    }
}
