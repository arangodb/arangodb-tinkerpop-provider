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

import java.util.Set;
import java.util.stream.Collectors;

import com.arangodb.tinkerpop.gremlin.process.filter.ArangoFilter;
import com.arangodb.tinkerpop.gremlin.process.filter.FilterSupport;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import org.apache.tinkerpop.gremlin.structure.Direction;


public class ArangoDBQueryBuilder {

    private ArangoDBQueryBuilder() {
    }

    public static String readVertexNeighbors(String graphName, Direction direction, ArangoDBGraphConfig config, String[] labels) {
        return oneStepTraversal(graphName, direction, config, labels)
                .append(" RETURN v")
                .toString();
    }

    public static String readVertexEdges(String graphName, Direction direction, ArangoDBGraphConfig config, String[] labels) {
        return oneStepTraversal(graphName, direction, config, labels)
                .append(" RETURN e")
                .toString();
    }

    private static StringBuilder oneStepTraversal(String graphName, Direction direction, ArangoDBGraphConfig config, String[] labels) {
        StringBuilder query = new StringBuilder()
                .append("FOR v, e IN 1..1 ")
                .append(toArangoDirection(direction))
                .append(" @vertexId GRAPH ")
                .append(escape(graphName))
                .append(" OPTIONS {edgeCollections: @edgeCollections}");
        if (labels.length > 0) {
            if (config.graphType == ArangoDBGraphConfig.GraphType.SIMPLE) {
                query.append(" FILTER e." + config.labelField + " IN @labels");
            } else {
                query.append(" FILTER PARSE_COLLECTION(e) IN @edgeCollections");
            }
        }
        return query;
    }

    static String readAllDocuments(Set<String> collections, ArangoFilter filter) {
        if (collections.isEmpty()) {
            throw new IllegalArgumentException();
        } else if (collections.size() == 1) {
            return readAllDocumentsFromSingleCollection(collections.iterator().next(), filter);
        } else {
            return readAllDocumentsFromMultipleCollections(collections, filter);
        }
    }

    private static String readAllDocumentsFromMultipleCollections(Set<String> collections, ArangoFilter filter) {
        String inner = collections.stream()
                .map(it -> ArangoDBQueryBuilder.readAllDocumentsFromSingleCollection(it, filter))
                .map(it -> "(" + it + ")")
                .collect(Collectors.joining(","));
        return String.format("FOR d in UNION(%s) RETURN d", inner);
    }

    private static String readAllDocumentsFromSingleCollection(String collection, ArangoFilter filter) {
        StringBuilder query = new StringBuilder()
                .append("FOR x IN ")
                .append(escape(collection));
        if (filter.getSupport() != FilterSupport.NONE) {
            query.append(" FILTER ").append(filter.toAql("x"));
        }
        query.append(" RETURN x");
        return query.toString();
    }

    private static String escape(String collection) {
        return String.format("`%s`", collection);
    }

    private static String toArangoDirection(final Direction direction) {
        switch (direction) {
            case BOTH:
                return "ANY";
            case IN:
                return "INBOUND";
            case OUT:
                return "OUTBOUND";
        }
        throw new IllegalArgumentException("Unsupported direction: " + direction);
    }

}
