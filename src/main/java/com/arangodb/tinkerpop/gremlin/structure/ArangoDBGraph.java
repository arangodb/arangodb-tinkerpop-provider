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

import java.util.*;

import com.arangodb.ArangoDatabase;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.tinkerpop.gremlin.PackageVersion;
import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.arangodb.tinkerpop.gremlin.persistence.ElementIdFactory;
import com.arangodb.tinkerpop.gremlin.persistence.VariablesData;
import com.arangodb.tinkerpop.gremlin.process.traversal.step.sideEffect.AQLStartStep;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoGraph;
import com.arangodb.tinkerpop.gremlin.client.ArangoDBGraphClient;
import com.arangodb.ArangoDB;

public class ArangoDBGraph implements Graph {
    public static final String GRAPH_VARIABLES_COLLECTION = "TINKERPOP-GRAPH-VARIABLES";
    private static final Logger LOGGER = LoggerFactory.getLogger(ArangoDBGraph.class);
    private static final Features FEATURES = new ArangoDBGraphFeatures();

    private final ArangoDBGraphClient client;
    private final ElementIdFactory idFactory;
    private final ArangoDBGraphConfig config;

    /**
     * Open a new {@code ArangoDBGraph} instance.
     * <p/>
     * This method is used by the {@link GraphFactory} to instantiate {@link Graph} instances.
     *
     * @param configuration the configuration for the instance
     * @return a newly opened {@link Graph}
     */
    public static ArangoDBGraph open(Configuration configuration) {
        return new ArangoDBGraph(configuration);
    }

    protected ArangoDBGraph(Configuration cfg) {
        LOGGER.debug("Creating new ArangoDB Graph from configuration");
        config = new ArangoDBGraphConfig(cfg);
        idFactory = ElementIdFactory.create(config);
        client = new ArangoDBGraphClient(config, idFactory, this);

        ArangoDatabase db = client.getArangoDatabase();
        if (!db.exists()) {
            if (config.enableDataDefinition) {
                db.create();
            } else {
                client.shutdown();
                throw new IllegalStateException("Database [" + db.name() + "] not found. To enable creation set: graph.enableDataDefinition=true");
            }
        }

        ArangoGraph graph = client.getArangoGraph();
        if (graph.exists()) {
            ArangoDBUtil.checkExistingGraph(graph.getInfo(), config);
        } else if (config.enableDataDefinition) {
            client.createGraph(name(), config.edgeDefinitions, config.orphanCollections);
        } else {
            client.shutdown();
            throw new IllegalStateException("Graph [" + graph.name() + "] not found. To enable creation set: graph.enableDataDefinition=true");
        }

        client.ensureVariablesDataCollection();
        VariablesData variablesData = Optional
                .ofNullable(client.getGraphVariables())
                .orElseGet(() -> client.insertGraphVariables(new VariablesData(name(), PackageVersion.VERSION)));
        new ArangoDBGraphVariables(this, variablesData).updateVersion();
    }

    public Set<String> edgeCollections() {
        return config.edges;
    }

    public Set<String> vertexCollections() {
        return config.vertices;
    }

    public ArangoDBGraphConfig.GraphType type() {
        return config.graphType;
    }

    @Override
    public Vertex addVertex(Object... keyValues) {
        ElementHelper.legalPropertyKeyValueArray(keyValues);
        String label = ElementHelper.getLabelValue(keyValues).orElse(null);
        Object id = ElementHelper.getIdValue(keyValues).orElse(null);
        ElementId elementId = idFactory.createVertexId(label, id);
        for (int i = 0; i < keyValues.length; i = i + 2) {
            if (keyValues[i] instanceof String) {
                ArangoDBUtil.validateProperty((String) keyValues[i], keyValues[i + 1]);
            }
        }
        ArangoDBVertex vertex = ArangoDBVertex.of(label, elementId, this);
        if (!config.vertices.contains(vertex.collection())) {
            throw new IllegalArgumentException(String.format("Vertex collection (%s) not in graph (%s).", vertex.collection(), name()));
        }

        vertex.doInsert();
        ElementHelper.attachProperties(vertex, keyValues);
        return vertex;
    }

    @Override
    public void close() {
        client.shutdown();
    }


    @Override
    public GraphComputer compute() throws IllegalArgumentException {
        throw Graph.Exceptions.graphComputerNotSupported();
    }

    @Override
    public <C extends GraphComputer> C compute(Class<C> graphComputerClass) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration configuration() {
        return config.configuration;
    }

    @Override
    public Iterator<Edge> edges(Object... edgeIds) {
        return getClient().getGraphEdges(idFactory.parseEdgeIds(edgeIds)).stream()
                .map(it -> (Edge) new ArangoDBEdge(this, it))
                .iterator();
    }

    @Override
    public Iterator<Vertex> vertices(Object... vertexIds) {
        return getClient().getGraphVertices(idFactory.parseVertexIds(vertexIds)).stream()
                .map(it -> (Vertex) new ArangoDBVertex(this, it))
                .iterator();
    }

    @Override
    public Features features() {
        return FEATURES;
    }

    public ArangoDBGraphClient getClient() {
        return client;
    }

    ElementIdFactory getIdFactory() {
        return idFactory;
    }

    /**
     * Create the {@link ElementId} for a persistent element.
     * The returned elementId can be used to reference the related ArangoDB document in AQL queries.
     *
     * @param element a persistent element (Edge or Vertex)
     * @return elementId
     */
    public ElementId elementId(Element element) {
        Objects.requireNonNull(element);
        if (element instanceof ArangoDBPersistentElement) {
            return ((ArangoDBPersistentElement) element).elementId();
        } else if (element instanceof Vertex) {
            return idFactory.createVertexId(element.label(), element.id());
        } else if (element instanceof Edge) {
            return idFactory.createEdgeId(element.label(), element.id());
        } else {
            throw new IllegalArgumentException("Unsupported element type: " + element.getClass().getName());
        }
    }

    public String name() {
        return config.graphName;
    }

    @Override
    public Transaction tx() {
        throw Graph.Exceptions.transactionsNotSupported();
    }

    @Override
    public Variables variables() {
        return new ArangoDBGraphVariables(this, client.getGraphVariables());
    }

    @Override
    public String toString() {
        return StringFactory.graphString(this, config.toString());
    }

    /**
     * Execute the AQL query and get the result set as a {@link GraphTraversal}.
     *
     * @param query the AQL query to execute
     * @return a fluent Gremlin traversal
     */
    public <E> GraphTraversal<?, E> aql(final String query) {
        return aql(query, Collections.emptyMap());
    }

    /**
     * Execute the AQL query and get the result set as a {@link GraphTraversal}.
     *
     * @param query   the AQL query to execute
     * @param options query options
     * @return a fluent Gremlin traversal
     */
    public <E> GraphTraversal<?, E> aql(final String query, final AqlQueryOptions options) {
        return aql(query, Collections.emptyMap(), options);
    }

    /**
     * Execute the AQL query with provided parameters and get the result set as a {@link GraphTraversal}.
     *
     * @param query      the AQL query to execute
     * @param parameters the parameters of the AQL query
     * @return a fluent Gremlin traversal
     */
    public <E> GraphTraversal<?, E> aql(final String query, final Map<String, ?> parameters) {
        return aql(query, parameters, new AqlQueryOptions());
    }

    /**
     * Execute the AQL query with provided parameters and get the result set as a {@link GraphTraversal}.
     *
     * @param query      the AQL query to execute
     * @param parameters the parameters of the AQL query
     * @param options    query options
     * @return a fluent Gremlin traversal
     */
    public <E> GraphTraversal<?, E> aql(final String query, final Map<String, ?> parameters, final AqlQueryOptions options) {
        GraphTraversal.Admin<?, E> traversal = new DefaultGraphTraversal<>(this);
        traversal.addStep(new AQLStartStep(traversal, query, client.query(query, parameters, options)));
        return traversal;
    }

    String getPrefixedCollectionName(String collectionName) {
        if (collectionName.startsWith(config.graphName + "_")) {
            return collectionName;
        }
        return config.graphName + "_" + collectionName;
    }

    /**
     * Get the underlying ArangoDB driver instance.
     *
     * @return ArangoDB driver instance
     */
    public ArangoDB getArangoDriver() {
        return client.getArangoDriver();
    }

    /**
     * Get the underlying ArangoDB database instance.
     *
     * @return ArangoDatabase instance
     */
    public ArangoDatabase getArangoDatabase() {
        return client.getArangoDatabase();
    }

    /**
     * Get the underlying ArangoGraph instance.
     *
     * @return ArangoGraph instance
     */
    public ArangoGraph getArangoGraph() {
        return client.getArangoGraph();
    }
}
