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

package com.arangodb.tinkerpop.gremlin.client;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.arangodb.*;
import com.arangodb.entity.*;
import com.arangodb.internal.ArangoDefaults;
import com.arangodb.internal.serde.ContentTypeFactory;
import com.arangodb.model.*;
import com.arangodb.serde.jackson.JacksonMapperProvider;
import com.arangodb.serde.jackson.JacksonSerde;
import com.arangodb.tinkerpop.gremlin.persistence.*;
import com.arangodb.tinkerpop.gremlin.persistence.serde.SerdeModule;
import com.arangodb.tinkerpop.gremlin.process.filter.ArangoFilter;
import com.arangodb.tinkerpop.gremlin.process.filter.EmptyFilter;
import com.arangodb.tinkerpop.gremlin.process.filter.FilterSupport;
import com.arangodb.tinkerpop.gremlin.structure.*;
import com.arangodb.tinkerpop.gremlin.utils.AqlDeserializer;
import com.fasterxml.jackson.databind.*;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalInterruptedException;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArangoDBGraphClient {

    private static final Logger logger = LoggerFactory.getLogger(ArangoDBGraphClient.class);

    protected final ArangoDatabase db;

    protected final ArangoDBGraphConfig config;

    private final AqlDeserializer aqlDeserializer;

    public ArangoDBGraphClient(ArangoDBGraphConfig config, ElementIdFactory idFactory, ArangoDBGraph graph) {
        logger.debug("Initiating the ArangoDb Client");
        this.config = config;
        Protocol protocol = config.driverConfig.getProtocol()
                .orElse(ArangoDefaults.DEFAULT_PROTOCOL);
        ObjectMapper mapper = JacksonMapperProvider
                .of(ContentTypeFactory.of(protocol))
                .registerModule(new SerdeModule(idFactory, config));
        aqlDeserializer = new AqlDeserializer(graph, mapper);
        db = new ArangoDB.Builder()
                .loadProperties(config.driverConfig)
                .serde(JacksonSerde.create(mapper))
                .build()
                .db(config.dbName);
    }

    public void shutdown() {
        logger.debug("Shutdown");
        db.arango().shutdown();
    }

    public void ensureVariablesDataCollection() {
        ArangoCollection col = db.collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION);
        if (!col.exists()) {
            col.create();
        }
    }

    public VariablesData getGraphVariables() {
        logger.debug("Get graph variables");
        try {
            return db
                    .collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION)
                    .getDocument(config.graphName, VariablesData.class);
        } catch (ArangoDBException e) {
            logger.error("Failed to retrieve graph variables", e);
            throw mapException(e);
        }
    }

    public VariablesData insertGraphVariables(VariablesData document) {
        logger.debug("Insert graph variables {} in {}", document, config.graphName);
        ArangoCollection col = db.collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION);
        try {
            col.insertDocument(document);
        } catch (ArangoDBException e) {
            logger.error("Failed to insert document", e);
            throw mapException(e);
        }
        return document;
    }

    public void updateGraphVariables(VariablesData document) {
        logger.debug("Update variables {} in {}", document, config.graphName);
        try {
            db
                    .collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION)
                    .replaceDocument(document.getKey(), document);
        } catch (ArangoDBException e) {
            throw mapException(e);
        }
    }

    public Stream<VertexData> getGraphVertices(List<ElementId> ids, ArangoFilter filter, Set<String> colNames) {
        logger.debug("Get all {} graph vertices, filtered by AQL filters", config.graphName);
        return getGraphDocuments(ids, filter, colNames, VertexData.class);
    }

    public Stream<EdgeData> getGraphEdges(List<ElementId> ids, ArangoFilter filter, Set<String> colNames) {
        logger.debug("Get all {} graph edges, filtered by AQL filters", config.graphName);
        return getGraphDocuments(ids, filter, colNames, EdgeData.class);
    }

    /**
     * Get vertices of a graph. If no ids are provided, get all vertices.
     *
     * @param ids the ids to match
     * @return the documents
     */
    public Stream<VertexData> getGraphVertices(final List<ElementId> ids) {
        logger.debug("Get all {} graph vertices, filtered by ids: {}", config.graphName, ids);
        return getGraphDocuments(ids, EmptyFilter.instance(), config.vertices, VertexData.class);
    }

    /**
     * Get edges of a graph. If no ids are provided, get all edges.
     *
     * @param ids the ids to match
     * @return the documents
     */
    public Stream<EdgeData> getGraphEdges(List<ElementId> ids) {
        logger.debug("Get all {} graph edges, filtered by ids: {}", config.graphName, ids);
        return getGraphDocuments(ids, EmptyFilter.instance(), config.edges, EdgeData.class);
    }

    private <V> Stream<V> getGraphDocuments(List<ElementId> ids, ArangoFilter filter, Set<String> colNames, Class<V> clazz) {
        if (ids.isEmpty()) {
            if (colNames.isEmpty()) {
                return Stream.empty();
            }
            return query(ArangoDBQueryBuilder.readAllDocuments(colNames, filter), clazz, null);
        } else {
            List<ElementId> prunedIds = ids.stream()
                    .filter(it -> colNames.contains(it.getCollection()))
                    .collect(Collectors.toList());
            StringBuilder query = new StringBuilder();
            query.append("FOR d IN DOCUMENT(@ids)");
            if (filter.getSupport() != FilterSupport.NONE) {
                query.append(" FILTER ").append(filter.toAql("d"));
            }
            query.append(" RETURN d");
            return query(query.toString(), clazz, Collections.singletonMap("ids", prunedIds));
        }
    }

    /**
     * Create a new graph.
     *
     * @param name              the name of the new graph
     * @param edgeDefinitions   the edge definitions for the graph
     * @param orphanCollections orphan collections
     */
    public void createGraph(
            String name,
            Set<ArangoDBGraphConfig.EdgeDef> edgeDefinitions,
            Set<String> orphanCollections
    ) {
        logger.debug("Creating graph {}", name);
        Set<EdgeDefinition> defs = edgeDefinitions.stream()
                .map(ArangoDBGraphConfig.EdgeDef::toDbDefinition)
                .collect(Collectors.toSet());
        logger.debug("Creating graph in database.");
        db.createGraph(name, defs, new GraphCreateOptions()
                .orphanCollections(orphanCollections.toArray(new String[0])));
    }

    /**
     * Get the underlying ArangoGraph instance.
     *
     * @return ArangoGraph instance
     */
    public ArangoGraph getArangoGraph() {
        return db.graph(config.graphName);
    }

    /**
     * Get the underlying ArangoDB database instance.
     *
     * @return ArangoDatabase instance
     */
    public ArangoDatabase getArangoDatabase() {
        return db;
    }

    /**
     * Get the underlying ArangoDB driver instance.
     *
     * @return ArangoDB driver instance
     */
    public ArangoDB getArangoDriver() {
        return db.arango();
    }

    public Stream<Object> query(final String query, final Map<String, ?> parameters, final AqlQueryOptions options) {
        return query(query, JsonNode.class, parameters, options)
                .map(aqlDeserializer::deserialize);
    }

    private <V> Stream<V> query(String query, Class<V> type, Map<String, ?> parameters) {
        return query(query, type, parameters, new AqlQueryOptions());
    }

    private <V> Stream<V> query(String query, Class<V> type, Map<String, ?> parameters, AqlQueryOptions options) {
        logger.debug("Executing AQL query: {}, with parameters: {}, with options: {}", query, parameters, options);
        try {
            return db.query(query, type, parameters, options).stream();
        } catch (ArangoDBException e) {
            throw mapException(e);
        }
    }

    public void insertEdge(ArangoDBEdge edge) {
        logger.debug("Insert edge {} in {} ", edge, config.graphName);
        EdgeEntity insertEntity;
        try {
            insertEntity = db.graph(config.graphName)
                    .edgeCollection(edge.collection())
                    .insertEdge(edge.data());
        } catch (ArangoDBException e) {
            throw mapException(e);
        }
        edge.update(insertEntity);
    }

    public void deleteEdge(ArangoDBEdge edge) {
        logger.debug("Delete edge {} in {}", edge, config.graphName);
        try {
            db.graph(config.graphName)
                    .edgeCollection(edge.collection())
                    .deleteEdge(edge.key());
        } catch (ArangoDBException e) {
            Integer errNum = e.getErrorNum();
            if (errNum != null && errNum == 1202) { // document not found
                return;
            }
            throw mapException(e);
        }
    }

    public void updateEdge(ArangoDBEdge edge) {
        logger.debug("Update edge {} in {}", edge, config.graphName);
        EdgeUpdateEntity updateEntity;
        try {
            updateEntity = db.graph(config.graphName)
                    .edgeCollection(edge.collection())
                    .replaceEdge(edge.key(), edge.data());
        } catch (ArangoDBException e) {
            throw mapException(e);
        }
        logger.debug("Edge updated, new rev {}", updateEntity.getRev());
        edge.update(updateEntity);
    }

    public VertexData readVertex(ElementId id) {
        logger.debug("Read vertex {} in {}", id, config.graphName);
        try {
            return db.graph(config.graphName)
                    .vertexCollection(id.getCollection())
                    .getVertex(id.getKey(), VertexData.class);
        } catch (ArangoDBException e) {
            throw mapException(e);
        }
    }

    public void insertVertex(ArangoDBVertex vertex) {
        logger.debug("Insert vertex {} in {}", vertex, config.graphName);
        VertexEntity vertexEntity;
        try {
            vertexEntity = db.graph(config.graphName)
                    .vertexCollection(vertex.collection())
                    .insertVertex(vertex.data());
        } catch (ArangoDBException e) {
            throw mapException(e);
        }
        vertex.update(vertexEntity);
    }

    public void deleteVertex(ArangoDBVertex vertex) {
        logger.debug("Delete vertex {} in {}", vertex, config.graphName);
        try {
            db.graph(config.graphName)
                    .vertexCollection(vertex.collection())
                    .deleteVertex(vertex.key());
        } catch (ArangoDBException e) {
            Integer errNum = e.getErrorNum();
            if (errNum != null && errNum == 1202) { // document not found
                return;
            }
            throw mapException(e);
        }
    }

    public void updateVertex(ArangoDBVertex vertex) {
        logger.debug("Update document {} in {}", vertex, config.graphName);
        VertexUpdateEntity vertexEntity;
        try {
            vertexEntity = db.graph(config.graphName)
                    .vertexCollection(vertex.collection())
                    .replaceVertex(vertex.key(), vertex.data());
        } catch (ArangoDBException e) {
            throw mapException(e);
        }
        logger.debug("Document updated, new rev {}", vertexEntity.getRev());
    }

    public Stream<VertexData> getVertexNeighbors(ElementId vertexId, Set<String> edgeCollections, Direction direction, String[] labels) {
        logger.debug("Get vertex {}:{} Neighbors, in {}, from collections {}", vertexId, direction, config.graphName, edgeCollections);
        String query = ArangoDBQueryBuilder.readVertexNeighbors(config.graphName, direction, config, labels);
        Map<String, Object> params = new HashMap<>();
        params.put("vertexId", vertexId);
        params.put("edgeCollections", edgeCollections);
        if (labels.length > 0 && config.graphType == ArangoDBGraphConfig.GraphType.SIMPLE) {
            params.put("labels", labels);
        }
        return query(query, VertexData.class, params);
    }

    public Stream<EdgeData> getVertexEdges(ElementId vertexId, Set<String> edgeCollections, Direction direction, String[] labels) {
        logger.debug("Get vertex {}:{} Edges, in {}, from collections {}", vertexId, direction, config.graphName, edgeCollections);
        String query = ArangoDBQueryBuilder.readVertexEdges(config.graphName, direction, config, labels);
        Map<String, Object> params = new HashMap<>();
        params.put("vertexId", vertexId);
        params.put("edgeCollections", edgeCollections);
        if (labels.length > 0 && config.graphType == ArangoDBGraphConfig.GraphType.SIMPLE) {
            params.put("labels", labels);
        }
        return query(query, EdgeData.class, params);
    }

    private RuntimeException mapException(ArangoDBException ex) {
        if (ex.getCause() instanceof InterruptedException) {
            TraversalInterruptedException ie = new TraversalInterruptedException();
            ie.initCause(ex);
            return ie;
        }
        Integer errNum = ex.getErrorNum();
        if (errNum != null && errNum == 1210) {
            return new IllegalArgumentException("Document with id already exists", ex);
        }
        return ex;
    }

}