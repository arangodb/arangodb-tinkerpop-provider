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

import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.arangodb.tinkerpop.gremlin.persistence.VertexData;
import com.arangodb.tinkerpop.gremlin.persistence.VertexPropertyData;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.arangodb.tinkerpop.gremlin.structure.ArangoDBElement.Exceptions.elementAlreadyRemoved;

public class ArangoDBVertex extends ArangoDBElement<VertexPropertyData, VertexData> implements Vertex, ArangoDBPersistentElement {

    static ArangoDBVertex of(String label, ElementId id, ArangoDBGraph graph) {
        String inferredLabel = label != null ? label : Optional.ofNullable(id.getLabel()).orElse(Vertex.DEFAULT_LABEL);
        return new ArangoDBVertex(graph, new VertexData(inferredLabel, id));
    }

    public ArangoDBVertex(ArangoDBGraph graph, VertexData data) {
        super(graph, data);
    }

    @Override
    public <V> VertexProperty<V> property(
            final VertexProperty.Cardinality cardinality,
            final String key,
            final V value,
            final Object... keyValues
    ) {
        if (removed()) throw elementAlreadyRemoved(id());
        ElementHelper.legalPropertyKeyValueArray(keyValues);
        ElementHelper.validateProperty(key, value);
        if (ElementHelper.getIdValue(keyValues).isPresent())
            throw VertexProperty.Exceptions.userSuppliedIdsNotSupported();

        if (cardinality != VertexProperty.Cardinality.single)
            throw VertexProperty.Exceptions.multiPropertiesNotSupported();

        VertexProperty<?> property = property(key);
        if (property.isPresent()) {
            property.remove();
        }

        VertexPropertyData prop = new VertexPropertyData(value);
        data.put(key, prop);
        doUpdate();

        ArangoDBVertexProperty<V> vertexProperty = new ArangoDBVertexProperty<>(key, prop, this);
        ElementHelper.attachProperties(vertexProperty, keyValues);
        return vertexProperty;
    }

    @Override
    public Edge addEdge(String label, Vertex vertex, Object... keyValues) {
        if (null == vertex) throw Graph.Exceptions.argumentCanNotBeNull("vertex");
        if (removed() || ((ArangoDBVertex) vertex).removed()) throw elementAlreadyRemoved(id());

        ElementHelper.legalPropertyKeyValueArray(keyValues);
        ElementHelper.validateLabel(label);
        Object id = ElementHelper.getIdValue(keyValues).orElse(null);
        ElementId elementId = graph.getIdFactory().createEdgeId(label, id);
        ElementId outVertexId = graph.getIdFactory().parseVertexId(id());
        ElementId inVertexId = graph.getIdFactory().parseVertexId(vertex.id());
        ArangoDBEdge edge = ArangoDBEdge.of(label, elementId, outVertexId, inVertexId, graph);
        if (!graph.edgeCollections().contains(edge.collection())) {
            throw new IllegalArgumentException(String.format("Edge collection (%s) not in graph (%s).", edge.collection(), graph.name()));
        }

        edge.doInsert();
        ElementHelper.attachProperties(edge, keyValues);
        return edge;
    }

    @Override
    protected void doRemove() {
        edges(Direction.BOTH).forEachRemaining(Edge::remove);
        graph.getClient().deleteVertex(this);
    }

    @Override
    protected String stringify() {
        return StringFactory.vertexString(this);
    }

    @Override
    protected <V> Property<V> createProperty(String key, VertexPropertyData value) {
        return new ArangoDBVertexProperty<>(key, value, this);
    }

    @Override
    public Iterator<Edge> edges(Direction direction, String... edgeLabels) {
        Set<String> edgeCollections = getQueryEdgeCollections(edgeLabels);
        // If edgeLabels was not empty but all were discarded, this means that we should
        // return an empty iterator, i.e. no edges for that edgeLabels exist.
        if (edgeCollections.isEmpty()) {
            return Collections.emptyIterator();
        }
        return IteratorUtils.map(graph.getClient().getVertexEdges(elementId(), edgeCollections, direction, edgeLabels),
                it -> new ArangoDBEdge(graph, it));
    }

    @Override
    public Iterator<Vertex> vertices(Direction direction, String... edgeLabels) {
        Set<String> edgeCollections = getQueryEdgeCollections(edgeLabels);
        // If edgeLabels was not empty but all were discarded, this means that we should
        // return an empty iterator, i.e. no edges for that edgeLabels exist.
        if (edgeCollections.isEmpty()) {
            return Collections.emptyIterator();
        }
        return IteratorUtils.map(graph.getClient().getVertexNeighbors(elementId(), edgeCollections, direction, edgeLabels),
                it -> new ArangoDBVertex(graph, it));
    }

    @Override
    public void doInsert() {
        graph.getClient().insertVertex(this);
    }

    @Override
    public void doUpdate() {
        graph.getClient().updateVertex(this);
    }

    @Override
    public <V> VertexProperty<V> property(final String key) {
        return Vertex.super.property(key);
    }

    @Override
    public <V> VertexProperty<V> property(final String key, final V value) {
        return property(VertexProperty.Cardinality.single, key, value);
    }

    @Override
    public <V> Iterator<VertexProperty<V>> properties(String... propertyKeys) {
        return IteratorUtils.cast(super.properties(propertyKeys));
    }

    void removeProperty(ArangoDBVertexProperty<?> prop) {
        if (removed()) throw ArangoDBElement.Exceptions.elementAlreadyRemoved(id());
        data.remove(prop.key());
        doUpdate();
    }

    private Set<String> getQueryEdgeCollections(String... edgeLabels) {
        if (graph.type() == ArangoDBGraphConfig.GraphType.SIMPLE || edgeLabels.length == 0) {
            return graph.edgeCollections();
        }
        return Arrays.stream(edgeLabels)
                .map(graph::getPrefixedCollectionName)
                .filter(graph.edgeCollections()::contains)
                .collect(Collectors.toSet());
    }
}
