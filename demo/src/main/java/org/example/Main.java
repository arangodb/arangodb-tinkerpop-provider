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

package org.example;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.util.List;
import java.util.Map;

import static org.apache.tinkerpop.gremlin.process.traversal.Order.desc;
import static org.apache.tinkerpop.gremlin.process.traversal.Scope.local;
import static org.apache.tinkerpop.gremlin.structure.Column.values;

/**
 * Demo program showing how to use Gremlin with ArangoDB TinkerPop provider.
 * Based on: Practical Gremlin: An Apache TinkerPop Tutorial by Kelvin R. Lawrence
 * (<a href=https://www.kelvinlawrence.net/book/PracticalGremlin.html>link</a>)
 */
public class Main {
    private static final String DB_NAME = "demo";
    private static final String GRAPHML_FILE = "src/main/resources/air-routes-small.graphml";

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        System.out.println("Starting ArangoDB TinkerPop Demo with Air Routes Data");

        //region cleanup
        System.out.println("Cleaning up existing database:");
        {
            ArangoDatabase db = new ArangoDB.Builder()
                    .host("172.28.0.1", 8529)
                    .password("test")
                    .build()
                    .db(DB_NAME);
            if (db.exists()) {
                db.drop();
            }
            db.arango().shutdown();
        }
        //endregion

        // create Tinkerpop graph backed by ArangoDB
        Configuration conf = new ArangoDBConfigurationBuilder()
                .hosts("172.28.0.1:8529")
                .user("root")
                .password("test")
                .database(DB_NAME)
                .enableDataDefinition(true)
                .build();
        ArangoDBGraph graph = ArangoDBGraph.open(conf);
        GraphTraversalSource g = graph.traversal();

        // print supported features
        System.out.println("Graph Features:");
        System.out.println(graph.features());

        // Import GraphML data
        System.out.println("\nImporting Air Routes data from GraphML file...");
        {
            g.io(Main.GRAPHML_FILE).read().iterate();
            System.out.println("Data import completed.");
        }

        //region Basic Gremlin Queries
        System.out.println("\n=== Basic Gremlin Queries ===");
        {
            System.out.println("Counting vertices and edges:");
            long vertexCount = g.V().count().next();
            long edgeCount = g.E().count().next();
            System.out.println("  Vertices: " + vertexCount);
            System.out.println("  Edges: " + edgeCount);

            System.out.println("\nVertex labels in the graph:");
            g.V().label().dedup().forEachRemaining(label -> System.out.println("  " + label));

            System.out.println("\nEdge labels in the graph:");
            g.E().label().dedup().forEachRemaining(label -> System.out.println("  " + label));

            System.out.println("\nSample of 5 airports:");
            g.V().hasLabel("airport").limit(5).valueMap().forEachRemaining(
                    vm -> System.out.println("  " + vm)
            );

            System.out.println("\nSample of 5 routes:");
            g.E().hasLabel("route").limit(5).valueMap().forEachRemaining(
                    vm -> System.out.println("  " + vm)
            );
        }
        //endregion

        //region Explore airports and routes
        System.out.println("\n=== Exploring Airports and Routes ===");
        {
            // Count airports by region
            System.out.println("\nTop 5 regions by number of airports:");
            g.V().hasLabel("airport")
                    .groupCount().by("region")
                    .order(local).by(values, desc)
                    .<Map.Entry<String, Long>>unfold().limit(5)
                    .forEachRemaining(e ->
                            System.out.println("  " + e.getKey() + ": " + e.getValue() + " airports"));


            // Find airports with most routes
            System.out.println("\nTop 5 airports with most outgoing routes:");
            g.V().hasLabel("airport")
                    .project("airport", "code", "routes")
                    .by("city")
                    .by("code")
                    .by(__.outE("route").count())
                    .order().by("routes", desc)
                    .limit(5)
                    .forEachRemaining(m ->
                            System.out.println("  " + m.get("airport") + " (" + m.get("code") + "): " + m.get("routes") + " routes"));
        }
        //endregion

        //region Graph Algorithms
        System.out.println("\n=== Graph Algorithms ===");
        {
            // Degree centrality - find most connected airports
            System.out.println("\nTop 5 airports by degree centrality (most connections):");
            g.V().hasLabel("airport")
                    .project("airport", "code", "degree")
                    .by("city")
                    .by("code")
                    .by(__.bothE().count())
                    .order().by("degree", desc)
                    .limit(5)
                    .forEachRemaining(m ->
                            System.out.println("  " + m.get("airport") + " (" + m.get("code") + "): " + m.get("degree") + " connections"));

            // Find a path between two airports
            System.out.println("\nFinding shortest path between Boston (BOS) and Atlanta (ATL):");

            g.V().hasLabel("airport")
                    .has("code", "BOS")
                    .repeat(__.out().simplePath())
                    .until(__.has("code", "ATL"))
                    .limit(5)
                    .path().by("code")
                    .forEachRemaining(path ->
                            System.out.println("  Path (" + (path.size() - 1) + " hops): " +
                                    String.join(" -> ", path.objects().stream().map(Object::toString).toList())));

            // Find all airports reachable within 2 hops from Long Beach
            System.out.println("\nCount of the airports reachable within 2 hops from Long Beach (LGB):");
            Long count = g.V().has("code", "LGB")
                    .repeat(__.out().simplePath())
                    .times(2)
                    .dedup()
                    .count()
                    .next();
            System.out.println("  " + count + " airports");
        }
        //endregion

        //region AQL Queries
        System.out.println("\n=== AQL Queries ===");
        {
            // Find weighted k-shortest paths between two airports with an AQL query
            System.out.println("\nFinding weighted k-shortest paths between Boston (BOS) and San Francisco (SFO) with AQL query:");

            String shortestPathQuery = """
                    LET start = FIRST(
                      FOR d IN tinkerpop_vertex
                      FILTER d.code == @start
                      RETURN d
                    )
                    
                    LET target = FIRST(
                      FOR d IN tinkerpop_vertex
                      FILTER d.code == @target
                      RETURN d
                    )
                    
                    FOR path IN OUTBOUND K_SHORTEST_PATHS start TO target GRAPH tinkerpop
                      OPTIONS { weightAttribute: 'dist' }
                      LIMIT 5
                      RETURN {
                        path: path.vertices[*].code,
                        dist: path.weight
                      }
                    """;

            graph.<Map<String, ?>>aql(shortestPathQuery, Map.of(
                    "start", "BOS",
                    "target", "SFO"
            )).forEachRemaining(path ->
                    System.out.println("  Path (dist: " + path.get("dist") + "): \t" + path.get("path")));

            // AQL traversal to find paths between two airports with constraints on edges
            System.out.println("\nFinding path between Boston (BOS) and Atlanta (ATL) with flights of max 400 km with AQL query:");

            String traversalQuery = """
                    LET start = FIRST(
                      FOR d IN tinkerpop_vertex
                      FILTER d.code == @start
                      RETURN d
                    )
                    
                    FOR v, e, p IN 1..10 OUTBOUND start GRAPH tinkerpop
                      PRUNE e.dist > 400 || v.code == @target
                      OPTIONS { uniqueVertices: 'path', order: 'bfs' }
                      FILTER e.dist <= 400
                      FILTER v.code == @target
                      LIMIT 5
                      RETURN p
                    """;

            graph.<Map<String, ?>>aql(traversalQuery, Map.of(
                            "start", "BOS",
                            "target", "ATL"
                    ))
                    .project("path", "distances", "tot")
                    .by(__.select("vertices").unfold().values("code").fold())
                    .by(__.select("edges").unfold().values("dist").fold())
                    .by(__.select("edges").unfold().values("dist").sum())
                    .forEachRemaining(it -> {
                                @SuppressWarnings("unchecked")
                                List<String> path = (List<String>) it.get("path");
                                @SuppressWarnings("unchecked")
                                List<Number> distances = (List<Number>) it.get("distances");
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < distances.size(); i++) {
                                    sb.append(path.get(i)).append(" -(").append(distances.get(i)).append(")-> ");
                                }
                                sb.append(path.getLast());
                                System.out.println("  Path (dist: " + it.get("tot") + "): \t" + sb);
                            }
                    );
        }
        //endregion

        graph.close();
    }

}