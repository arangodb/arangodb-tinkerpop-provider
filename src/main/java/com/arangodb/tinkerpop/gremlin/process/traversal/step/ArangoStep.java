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

package com.arangodb.tinkerpop.gremlin.process.traversal.step;

import com.arangodb.tinkerpop.gremlin.process.filter.*;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBEdge;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBVertex;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil;
import com.arangodb.tinkerpop.gremlin.utils.Fields;
import org.apache.tinkerpop.gremlin.process.traversal.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.HasContainerHolder;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GraphStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.util.ConnectiveP;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ArangoStep<S, E extends Element> extends GraphStep<S, E> implements HasContainerHolder {

    private final List<HasContainer> hasContainers = new ArrayList<>();

    public ArangoStep(final GraphStep<S, E> originalGraphStep) {
        super(originalGraphStep.getTraversal(), originalGraphStep.getReturnClass(), originalGraphStep.isStartStep(), originalGraphStep.getIds());
        originalGraphStep.getLabels().forEach(this::addLabel);
        setIteratorSupplier(this::elements);
    }

    @Override
    public String toString() {
        if (hasContainers.isEmpty())
            return super.toString();
        else
            return 0 == ids.length ?
                    StringFactory.stepString(this, returnClass.getSimpleName().toLowerCase(), hasContainers) :
                    StringFactory.stepString(this, returnClass.getSimpleName().toLowerCase(), Arrays.toString(ids), hasContainers);
    }

    @Override
    public List<HasContainer> getHasContainers() {
        return Collections.unmodifiableList(hasContainers);
    }

    @Override
    public void addHasContainer(final HasContainer hasContainer) {
        normalizePredicate(hasContainer.getPredicate());
        hasContainers.add(hasContainer);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ hasContainers.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ArangoStep<?, ?> that = (ArangoStep<?, ?>) o;
        return Objects.equals(hasContainers, that.hasContainers);
    }

    private void normalizePredicate(P<?> p) {
        if (p instanceof ConnectiveP) {
            ((ConnectiveP<?>) p).getPredicates().forEach(this::normalizePredicate);
        } else {
            p.setValue(ArangoDBUtil.normalizeValue(p.getValue()));
        }
    }

    private String mapKey(String key, ArangoDBGraphConfig config) {
        if (key.equals(T.label.getAccessor())) {
            return config.labelField;
        } else if (key.equals(T.id.getAccessor())) {
            if (config.graphType == ArangoDBGraphConfig.GraphType.SIMPLE) {
                return Fields.KEY;
            } else {
                return Fields.ID;
            }
        } else {
            return key;
        }
    }

    private ArangoFilter getArangoFilter(ArangoDBGraphConfig config) {
        return AndFilter.of(hasContainers.stream()
                .filter(it -> it.getKey() != null)
                .filter(it -> config.graphType != ArangoDBGraphConfig.GraphType.COMPLEX || !T.label.getAccessor().equals(it.getKey()))
                .map(it -> ArangoFilter.of(mapKey(it.getKey(), config), it.getPredicate()))
                .filter(it -> it.getSupport() != FilterSupport.NONE)
                .collect(Collectors.toList()));
    }

    private Set<String> getCollections(ArangoDBGraphConfig config) {
        Set<String> collections;
        if (Vertex.class.isAssignableFrom(returnClass)) {
            collections = config.vertices;
        } else if (Edge.class.isAssignableFrom(returnClass)) {
            collections = config.edges;
        } else {
            throw new UnsupportedOperationException("Unsupported return type: " + returnClass);
        }

        if (config.graphType == ArangoDBGraphConfig.GraphType.SIMPLE) {
            return collections;
        }

        @SuppressWarnings("unchecked")
        List<? extends P<String>> labelFilters = hasContainers.stream()
                .filter(it -> T.label.getAccessor().equals(it.getKey()))
                .map(it -> (P<String>) it.getPredicate())
                .collect(Collectors.toList());

        return collections.stream()
                .filter(it -> labelFilters.stream().allMatch(x -> x.test(it)))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private Iterator<E> elements() {
        if (null == ids)
            return Collections.emptyIterator();

        @SuppressWarnings("resource")
        ArangoDBGraph graph = (ArangoDBGraph) getTraversal().getGraph().orElseThrow(IllegalStateException::new);
        ArangoDBGraphConfig config = graph.config;
        convertElementsToIds();
        Stream<E> res;
        if (Vertex.class.isAssignableFrom(returnClass)) {
            res = graph.getClient().getGraphVertices(graph.getIdFactory().parseVertexIds(ids), getArangoFilter(config), getCollections(config))
                    .map(it -> (E) new ArangoDBVertex(graph, it));
        } else if (Edge.class.isAssignableFrom(returnClass)) {
            res = graph.getClient().getGraphEdges(graph.getIdFactory().parseEdgeIds(ids), getArangoFilter(config), getCollections(config))
                    .map(it -> (E) new ArangoDBEdge(graph, it));
        } else {
            throw new UnsupportedOperationException("Unsupported return type: " + returnClass);
        }

        return res
                .filter(it -> HasContainer.testAll(it, hasContainers))
                .iterator();
    }

}
