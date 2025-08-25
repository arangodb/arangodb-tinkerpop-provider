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

package com.arangodb.tinkerpop.gremlin.persistence.simple;

import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.arangodb.tinkerpop.gremlin.persistence.ElementIdFactory;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;


public class SimpleElementIdFactory extends ElementIdFactory {

    private final String defaultVertexCollection;
    private final String defaultEdgeCollection;

    public SimpleElementIdFactory(ArangoDBGraphConfig config) {
        super(config);
        defaultVertexCollection = config.vertices.iterator().next();
        defaultEdgeCollection = config.edges.iterator().next();
    }

    @Override
    protected String defaultVertexCollection() {
        return defaultVertexCollection;
    }

    @Override
    protected String defaultEdgeCollection() {
        return defaultEdgeCollection;
    }

    @Override
    protected String inferCollection(final String collection, final String label, final String defaultCollection) {
        return defaultCollection;
    }

    @Override
    protected void validateId(String id, String label) {
        if (id.contains("/")) {
            throw new IllegalArgumentException(String.format("id (%s) contains invalid character '/'", id));
        }
    }

    @Override
    protected ElementId doCreate(String collection, String key) {
        return new SimpleId(collection, key);
    }
}
