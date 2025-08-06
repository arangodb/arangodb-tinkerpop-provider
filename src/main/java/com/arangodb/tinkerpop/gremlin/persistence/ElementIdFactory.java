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

import com.arangodb.tinkerpop.gremlin.persistence.complex.ComplexElementIdFactory;
import com.arangodb.tinkerpop.gremlin.persistence.simple.SimpleElementIdFactory;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public abstract class ElementIdFactory {
    protected final ArangoDBGraphConfig config;

    public static ElementIdFactory create(ArangoDBGraphConfig config) {
        switch (config.graphType) {
            case SIMPLE:
                return new SimpleElementIdFactory(config);
            case COMPLEX:
                return new ComplexElementIdFactory(config);
            default:
                throw new IllegalArgumentException("Unsupported graph type: " + config.graphType);
        }
    }

    protected ElementIdFactory(ArangoDBGraphConfig config) {
        this.config = config;
    }

    protected abstract String defaultVertexCollection();

    protected abstract String defaultEdgeCollection();

    protected abstract String inferCollection(final String collection, final String label, final String defaultCollection);

    protected abstract void validateId(String id);

    protected abstract ElementId doCreate(String prefix, String collection, String key);

    private String extractKey(final String id) {
        String[] parts = id.split("/");
        return parts[parts.length - 1];
    }

    private String extractCollection(final String id) {
        String[] parts = id.replaceFirst("^" + config.prefix, "").split("/");
        if (parts.length > 2) {
            throw new IllegalArgumentException(String.format("key (%s) contains invalid character '/'", id));
        }
        return parts.length == 2 ? parts[0] : null;
    }

    public ElementId createVertexId(String label, Object id) {
        return createId(label, id, defaultVertexCollection());
    }

    public ElementId createEdgeId(String label, Object id) {
        return createId(label, id, defaultEdgeCollection());
    }

    public ElementId parseVertexId(Object id) {
        if (id instanceof String) {
            return parseWithDefaultCollection((String) id, defaultVertexCollection());
        } else if (id instanceof Element) {
            return parseVertexId(((Element) id).id());
        } else {
            throw Vertex.Exceptions.userSuppliedIdsOfThisTypeNotSupported();
        }
    }

    public List<ElementId> parseVertexIds(Object[] ids) {
        return Arrays.stream(ids)
                .map(this::parseVertexId)
                .collect(Collectors.toList());
    }

    public ElementId parseEdgeId(Object id) {
        if (id instanceof String) {
            return parseWithDefaultCollection((String) id, defaultEdgeCollection());
        } else if (id instanceof Element) {
            return parseEdgeId(((Element) id).id());
        } else {
            throw Edge.Exceptions.userSuppliedIdsOfThisTypeNotSupported();
        }
    }

    public List<ElementId> parseEdgeIds(Object[] ids) {
        return Arrays.stream(ids)
                .map(this::parseEdgeId)
                .collect(Collectors.toList());
    }

    public ElementId parseId(String id) {
        String collection = extractCollection(id);
        String key = extractKey(id);
        return of(config.graphName, collection, key);
    }

    private ElementId parseWithDefaultCollection(String id, String defaultCollection) {
        String collection = inferCollection(extractCollection(id), null, defaultCollection);
        String key = extractKey(id);
        return of(config.graphName, collection, key);
    }

    private ElementId createId(String label, Object nullableId, String defaultCollection) {
        if (nullableId == null) {
            return of(config.graphName, inferCollection(null, label, defaultCollection), null);
        }

        if (!(nullableId instanceof String)) {
            throw new UnsupportedOperationException("Vertex / Edge does not support user supplied identifiers of this type");
        }

        String id = (String) nullableId;
        validateId(id);
        return of(config.graphName, inferCollection(extractCollection(id), label, defaultCollection), extractKey(id));
    }

    private ElementId of(String graphName, String collection, String key) {
        Objects.requireNonNull(graphName);
        Objects.requireNonNull(collection);
        ElementId.validateIdParts(graphName, collection, key);
        return doCreate(graphName, collection, key);
    }
}
