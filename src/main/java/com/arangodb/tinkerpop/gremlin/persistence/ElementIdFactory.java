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
    protected final String prefix;

    public static ElementIdFactory create(ArangoDBGraphConfig config) {
        switch (config.graphType) {
            case SIMPLE:
                return new SimpleElementIdFactory(config.graphName);
            case COMPLEX:
                return new ComplexElementIdFactory(config.graphName);
            default:
                throw new IllegalArgumentException("Unsupported graph type: " + config.graphType);
        }
    }

    protected ElementIdFactory(String prefix) {
        this.prefix = prefix;
    }

    protected abstract String inferCollection(final String collection, final String label, final String defaultLabel);

    protected abstract void validateId(String id);

    protected abstract ElementId doCreate(String prefix, String collection, String key);

    private String extractKey(final String id) {
        String[] parts = id.split("/");
        return parts[parts.length - 1];
    }

    private String extractCollection(final String id) {
        String[] parts = id.replaceFirst("^" + prefix + "_", "").split("/");
        if (parts.length > 2) {
            throw new IllegalArgumentException(String.format("key (%s) contains invalid character '/'", id));
        }
        return parts.length == 2 ? parts[0] : null;
    }

    public ElementId createVertexId(String label, Object id) {
        return createId(label, Vertex.DEFAULT_LABEL, id);
    }

    public ElementId createEdgeId(String label, Object id) {
        return createId(label, Edge.DEFAULT_LABEL, id);
    }

    public ElementId parseVertexId(Object id) {
        if (id instanceof String) {
            return parseWithDefaultLabel((String) id, Vertex.DEFAULT_LABEL);
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
            return parseWithDefaultLabel((String) id, Edge.DEFAULT_LABEL);
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
        return of(prefix, collection, key);
    }

    private ElementId parseWithDefaultLabel(String id, String defaultLabel) {
        String collection = inferCollection(extractCollection(id), null, defaultLabel);
        String key = extractKey(id);
        return of(prefix, collection, key);
    }

    private ElementId createId(String label, String defaultLabel, Object nullableId) {
        if (nullableId == null) {
            return of(prefix, inferCollection(null, label, defaultLabel), null);
        }

        if (!(nullableId instanceof String)) {
            throw new UnsupportedOperationException("Vertex / Edge does not support user supplied identifiers of this type");
        }

        String id = (String) nullableId;
        validateId(id);
        return of(prefix, inferCollection(extractCollection(id), label, defaultLabel), extractKey(id));
    }

    private ElementId of(String prefix, String collection, String key) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(collection);
        ElementId.validateIdParts(prefix, collection, key);
        return doCreate(prefix, collection, key);
    }
}
