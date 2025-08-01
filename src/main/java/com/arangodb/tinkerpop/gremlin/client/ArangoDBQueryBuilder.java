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

import org.apache.tinkerpop.gremlin.structure.Direction;

import static com.arangodb.tinkerpop.gremlin.utils.Fields.LABEL;


public class ArangoDBQueryBuilder {

    private ArangoDBQueryBuilder() {
    }

    public static String readVertexNeighbors(String graphName, Direction direction, String[] labels) {
        return oneStepTraversal(graphName, direction, labels)
                .append(" RETURN v")
                .toString();
    }

    public static String readVertexEdges(String graphName, Direction direction, String[] labels) {
        return oneStepTraversal(graphName, direction, labels)
                .append(" RETURN e")
                .toString();
    }

    private static StringBuilder oneStepTraversal(String graphName, Direction direction, String[] labels) {
        StringBuilder query = new StringBuilder()
                .append("FOR v, e IN 1..1 ")
                .append(toArangoDirection(direction))
                .append(" @vertexId GRAPH ")
                .append(escape(graphName))
                .append(" OPTIONS {edgeCollections: @edgeCollections}");
        if (labels.length > 0) {
            query.append(" FILTER e." + LABEL + " IN @labels");
        }
        return query;
    }

    public static String readAllDocuments(Set<String> collections) {
        if (collections.isEmpty()) {
            throw new IllegalArgumentException();
        } else if (collections.size() == 1) {
            return readAllDocumentsFromSingleCollection(collections.iterator().next());
        } else {
            return readAllDocumentsFromMultipleCollections(collections);
        }
    }

    private static String readAllDocumentsFromMultipleCollections(Set<String> collections) {
        String inner = collections.stream()
                .map(ArangoDBQueryBuilder::readAllDocumentsFromSingleCollection)
                .map(it -> "(" + it + ")")
                .collect(Collectors.joining(","));
        return String.format("FOR d in UNION(%s) RETURN d", inner);
    }

    private static String readAllDocumentsFromSingleCollection(String collection) {
        return String.format("FOR x IN %s RETURN x", escape(collection));
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
