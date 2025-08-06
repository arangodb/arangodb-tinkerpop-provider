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

import com.arangodb.tinkerpop.gremlin.persistence.EdgeData;
import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.arangodb.tinkerpop.gremlin.persistence.VertexData;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

import java.util.*;


public class ArangoDBEdge extends ArangoDBSimpleElement<EdgeData> implements Edge, ArangoDBPersistentElement {

    static ArangoDBEdge of(String label, ElementId id, ElementId outVertexId, ElementId inVertexId, ArangoDBGraph graph) {
        String inferredLabel = label != null ? label : Optional.ofNullable(id.getLabel()).orElse(Edge.DEFAULT_LABEL);
        return new ArangoDBEdge(graph, new EdgeData(inferredLabel, id, outVertexId, inVertexId));
    }

    public ArangoDBEdge(ArangoDBGraph graph, EdgeData data) {
        super(graph, data);
    }

    @Override
    protected void doRemove() {
        graph.getClient().deleteEdge(this);
    }

    @Override
    protected void doUpdate() {
        graph.getClient().updateEdge(this);
    }

    @Override
    public void doInsert() {
        graph.getClient().insertEdge(this);
    }

    @Override
    protected String stringify() {
        return "e[" + id() + "][" + data.getFrom().getId() + "-" + label() + "->" + data.getTo().getId() + "]";
    }

    @Override
    public Vertex outVertex() {
        return vertex(data.getFrom());
    }

    @Override
    public Vertex inVertex() {
        return vertex(data.getTo());
    }

    private Vertex vertex(ElementId eId) {
        if (removed()) throw ArangoDBElement.Exceptions.elementAlreadyRemoved(id());
        VertexData v = graph.getClient().readVertex(eId);
        if (v == null) {
            throw ArangoDBElement.Exceptions.elementAlreadyRemoved(eId.getId());
        }
        return new ArangoDBVertex(graph, v);
    }

    @Override
    public Iterator<Vertex> vertices(final Direction direction) {
        if (removed()) return Collections.emptyIterator();
        switch (direction) {
            case OUT:
                return IteratorUtils.of(this.outVertex());
            case IN:
                return IteratorUtils.of(this.inVertex());
            default:
                return IteratorUtils.of(this.outVertex(), this.inVertex());
        }
    }

    @Override
    public <V> Iterator<Property<V>> properties(final String... propertyKeys) {
        return IteratorUtils.cast(super.properties(propertyKeys));
    }
}
