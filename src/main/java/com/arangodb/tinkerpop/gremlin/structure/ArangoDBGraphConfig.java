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

import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.tinkerpop.gremlin.utils.Fields;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

public class ArangoDBGraphConfig {
    public static final String KEY_PREFIX = "gremlin.arangodb.conf";
    public static final String KEY_DRIVER_PREFIX = "driver";

    // configuration properties keys
    public static final String KEY_DB_NAME = "graph.db";
    public static final String KEY_GRAPH_NAME = "graph.name";
    public static final String KEY_GRAPH_TYPE = "graph.type";
    public static final String KEY_GRAPH_ORPHAN_COLLECTIONS = "graph.orphanCollections";
    public static final String KEY_GRAPH_EDGE_DEFINITIONS = "graph.edgeDefinitions";
    public static final String KEY_ENABLE_DATA_DEFINITION = "graph.enableDataDefinition";
    public static final String KEY_LABEL_FIELD = "graph.labelField";

    // default values
    public static final String DEFAULT_DB_NAME = "_system";
    public static final String DEFAULT_GRAPH_NAME = "tinkerpop";
    public static final GraphType DEFAULT_GRAPH_TYPE = GraphType.SIMPLE;
    public static final boolean DEFAULT_ENABLE_DATA_DEFINITION = false;
    public static final String DEFAULT_LABEL_FIELD = "_label";

    public final Configuration configuration;
    public final String dbName;
    public final String graphName;
    public final GraphType graphType;
    public final Set<String> orphanCollections;
    public final Set<EdgeDef> edgeDefinitions;
    public final Set<String> vertices;
    public final Set<String> edges;
    public final ArangoConfigProperties driverConfig;
    public final boolean enableDataDefinition;
    public final String labelField;

    public ArangoDBGraphConfig(Configuration configuration) {
        this.configuration = configuration;
        Configuration conf = configuration.subset(KEY_PREFIX);
        dbName = conf.getString(KEY_DB_NAME, DEFAULT_DB_NAME);
        graphName = conf.getString(KEY_GRAPH_NAME, DEFAULT_GRAPH_NAME);
        graphType = conf.getEnum(KEY_GRAPH_TYPE, GraphType.class, DEFAULT_GRAPH_TYPE);
        orphanCollections = computeOrphanCollections(conf.getList(String.class, KEY_GRAPH_ORPHAN_COLLECTIONS, Collections.emptyList()));
        edgeDefinitions = computeEdgeDefinitions(conf.getList(String.class, KEY_GRAPH_EDGE_DEFINITIONS, Collections.emptyList()));
        vertices = computeVertices();
        edges = edgeDefinitions.stream().map(EdgeDef::getCollection).collect(Collectors.toSet());
        driverConfig = ArangoConfigProperties.fromProperties(ConfigurationConverter.getProperties(conf.subset(KEY_DRIVER_PREFIX)), null);
        enableDataDefinition = conf.getBoolean(KEY_ENABLE_DATA_DEFINITION, DEFAULT_ENABLE_DATA_DEFINITION);
        labelField = conf.getString(KEY_LABEL_FIELD, DEFAULT_LABEL_FIELD);
        validate();
    }

    public boolean isReservedField(String key) {
        return labelField.equals(key) || Fields.ALL_STATIC.contains(key);
    }

    private void validate() {
        validateName(graphName);
        vertices.forEach(this::validateName);
        edges.forEach(this::validateName);
        if (graphType == GraphType.SIMPLE) {
            if (vertices.size() > 1) {
                throw new IllegalArgumentException("Simple graph allows only 1 vertex collection");
            }
            if (edges.size() > 1) {
                throw new IllegalArgumentException("Simple graph allows only 1 edge collection");
            }
        }
        Objects.requireNonNull(labelField, "Label field must not be null");
        if (labelField.isEmpty()) {
            throw new IllegalArgumentException("Label field must not be empty");
        }
    }

    private Set<String> computeOrphanCollections(List<String> orphanCollections) {
        return new HashSet<>(orphanCollections);
    }

    private Set<EdgeDef> computeEdgeDefinitions(List<String> edges) {
        Set<EdgeDef> res = edges.stream()
                .map(this::computeEdgeDefinition)
                .collect(groupingBy(EdgeDef::getCollection))
                .entrySet().stream()
                .map(it -> {
                    String[] froms = it.getValue().stream().flatMap(x -> x.getFrom().stream()).distinct().toArray(String[]::new);
                    String[] tos = it.getValue().stream().flatMap(x -> x.getTo().stream()).distinct().toArray(String[]::new);
                    return new EdgeDef(it.getKey())
                            .from(froms)
                            .to(tos);
                })
                .collect(Collectors.toSet());
        if (graphType == GraphType.SIMPLE && res.isEmpty()) {
            res.add(new EdgeDef(Edge.DEFAULT_LABEL)
                    .from(Vertex.DEFAULT_LABEL)
                    .to(Vertex.DEFAULT_LABEL));
        }
        return res;
    }

    private EdgeDef computeEdgeDefinition(String edge) {
        // match: e2:[a,b]->[c,d]
        String rePattern = "^(.*):\\[(.*)]->\\[(.*)]$";
        Pattern p = Pattern.compile(rePattern);
        Matcher m = p.matcher(edge.replaceAll("\\s+", ""));
        if (!m.find()) {
            throw new IllegalArgumentException("Invalid edge definition: " + edge);
        }

        String collection = m.group(1);
        String[] from = Stream.of(m.group(2).split(",")).toArray(String[]::new);
        String[] to = Stream.of(m.group(3).split(",")).toArray(String[]::new);
        return new EdgeDef(collection)
                .from(from)
                .to(to);
    }

    private Set<String> computeVertices() {
        Set<String> res = edgeDefinitions.stream()
                .flatMap(it -> Stream.concat(it.getFrom().stream(), it.getTo().stream()))
                .collect(Collectors.toSet());
        res.addAll(orphanCollections);
        return res;
    }

    private void validateName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name cannot be empty");
        }
    }

    @Override
    public String toString() {
        return "ArangoDBGraphConfig{" +
                "dbName='" + dbName + '\'' +
                ", graphName='" + graphName + '\'' +
                ", graphType=" + graphType +
                ", vertices=" + vertices +
                ", edges=" + edges +
                ", edgeDefinitions=" + edgeDefinitions +
                ", orphanCollections=" + orphanCollections +
                ", driverConfig=" + driverConfig +
                '}';
    }

    public enum GraphType {
        /**
         * Type of graph that allows only:
         * - 1 vertex collection
         * - 1 edge collection
         * Elements ids are strings without format constraints.
         */
        SIMPLE,

        /**
         * Type of graph that allows multiple vertex collections and multiple edge collections.
         * Elements ids have format constraint: `<graph>_<label>/<key>`, where:
         * - `<graph>` is the graph name
         * - `<label>` is the element label
         * - `<key>` is the db document key
         */
        COMPLEX
    }

    public static final class EdgeDef {
        private final String collection;
        private final Set<String> from = new HashSet<>();
        private final Set<String> to = new HashSet<>();

        public static EdgeDef of(String collection) {
            return new EdgeDef(collection);
        }

        public static EdgeDef of(EdgeDefinition def) {
            return new EdgeDef(def.getCollection())
                    .from(def.getFrom().toArray(new String[0]))
                    .to(def.getTo().toArray(new String[0]));
        }

        private EdgeDef(String collection) {
            this.collection = collection;
        }

        public EdgeDef from(String... from) {
            Collections.addAll(this.from, from);
            return this;
        }

        public EdgeDef to(String... to) {
            Collections.addAll(this.to, to);
            return this;
        }

        public String getCollection() {
            return collection;
        }

        public Set<String> getFrom() {
            return from;
        }

        public Set<String> getTo() {
            return to;
        }

        public EdgeDefinition toDbDefinition() {
            return new EdgeDefinition()
                    .collection(getCollection())
                    .from(getFrom().toArray(new String[0]))
                    .to(getTo().toArray(new String[0]));
        }

        @Override
        public String toString() {
            return collection + ":[" + String.join(",", from) + "]->[" + String.join(",", to) + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof EdgeDef)) return false;
            EdgeDef edgeDef = (EdgeDef) o;
            return Objects.equals(collection, edgeDef.collection) && Objects.equals(from, edgeDef.from) && Objects.equals(to, edgeDef.to);
        }

        @Override
        public int hashCode() {
            return Objects.hash(collection, from, to);
        }
    }

}
