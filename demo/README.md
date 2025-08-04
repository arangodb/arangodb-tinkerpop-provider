# ArangoDB TinkerPop Provider Demo

The ArangoDB TinkerPop Provider allows you to leverage the power of the Gremlin graph traversal language while using 
ArangoDB as the underlying storage engine.
This demo shows how to set up a simple graph using a small air routes dataset and perform various queries. 

## Requirements

This demo requires:
- JDK 17 or higher
- `maven`
- `docker`

## Prepare the environment

Start ArangoDB with docker:

```shell
../docker/start_db.sh
```

The deployed cluster will be accessible at [https://172.28.0.1:8529](http://172.28.0.1:8529) with username `root` and
password `test`.

## Install locally

NB: this is only needed for SNAPSHOT versions.

```shell
mvn -f ../pom.xml install -Dmaven.test.skip=true -Dgpg.skip=true -Dmaven.javadoc.skip=true
```

## Running the Demo

You can run the demo using Maven:

```shell
mvn exec:java -Dexec.mainClass="org.example.Main"
```

## Demo Walkthrough

### Create a TinkerPop Graph

We create a TinkerPop graph backed by ArangoDB using the `ArangoDBConfigurationBuilder` to set up the connection parameters:

```
// create Tinkerpop graph backed by ArangoDB
Configuration conf = new ArangoDBConfigurationBuilder() (1)
        .hosts("172.28.0.1:8529")                       (2)
        .user("root")
        .password("test")
        .database(DB_NAME)
        .enableDataDefinition(true)                     (3)
        .build();
ArangoDBGraph graph = ArangoDBGraph.open(conf);         (4)
GraphTraversalSource g = graph.traversal();             (5)
```

This code:
1. Creates a configuration using `ArangoDBConfigurationBuilder`
2. Specifies the ArangoDB host, user credentials and database name
3. Enables data definition (enables creating database and graph definition)
4. Creates the TinkerPop graph
5. Creates a graph traversal source

### Show Graph Features

We can check the supported features of the ArangoDB TinkerPop implementation:

```
// print supported features
System.out.println("Graph Features:");
System.out.println(graph.features());
```

**Console Output:**
```
Graph Features:
FEATURES
> GraphFeatures
>-- Computer: false
>-- Persistence: true
>-- ConcurrentAccess: true
>-- Transactions: false
>-- ThreadedTransactions: false
>-- IoRead: true
>-- IoWrite: true
...
(output truncated)
```

### Import GraphML Data

Next, we import air routes data from a local GraphML file:

```
private static final String GRAPHML_FILE = "src/main/resources/air-routes-small.graphml";

// ...

// Import GraphML data
System.out.println("\nImporting Air Routes data from GraphML file...");
{
    g.io(Main.GRAPHML_FILE).read().iterate();
    System.out.println("Data import completed.");
}
```

### Basic Gremlin Queries

After importing the data, we can run some basic Gremlin queries to explore the graph.

```
//region Basic Gremlin Queries
System.out.println("\n=== Basic Gremlin Queries ===");
{
    System.out.println("Counting vertices and edges:");
    long vertexCount = g.V().count().next();                    (1)
    long edgeCount = g.E().count().next();                      (1)
    System.out.println("  Vertices: " + vertexCount);
    System.out.println("  Edges: " + edgeCount);

    System.out.println("\nVertex labels in the graph:");
    g.V().label().dedup().forEachRemaining(label ->             (2)
        System.out.println("  " + label));

    System.out.println("\nEdge labels in the graph:");
    g.E().label().dedup().forEachRemaining(label ->             (2) 
        System.out.println("  " + label));

    System.out.println("\nSample of 5 airports:");
    g.V().hasLabel("airport").limit(5).valueMap()               (3)
        .forEachRemaining(vm -> 
            System.out.println("  " + vm));

    System.out.println("\nSample of 5 routes:");
    g.E().hasLabel("route").limit(5).valueMap()                 (3)
        .forEachRemaining(vm -> 
            System.out.println("  " + vm));
}
//endregion
```

This section:
1. Counts the number of vertices and edges in the graph
2. Lists all vertex and edge labels
3. Shows a sample of 5 vertices and 5 edges with their properties

**Console Output:**
```
=== Basic Gremlin Queries ===
Counting vertices and edges:
  Vertices: 46
  Edges: 1326

Vertex labels in the graph:
  airport

Edge labels in the graph:
  route

Sample of 5 airports:
  {country=[US], code=[ATL], longest=[12390], city=[Atlanta], elev=[1026], icao=[KATL], lon=[-84.4281005859375], type=[airport], region=[US-GA], runways=[5], lat=[33.6366996765137], desc=[Hartsfield - Jackson Atlanta International Airport]}
  {country=[US], code=[ANC], longest=[12400], city=[Anchorage], elev=[151], icao=[PANC], lon=[-149.996002197266], type=[airport], region=[US-AK], runways=[3], lat=[61.1744003295898], desc=[Anchorage Ted Stevens]}
  {country=[US], code=[AUS], longest=[12250], city=[Austin], elev=[542], icao=[KAUS], lon=[-97.6698989868164], type=[airport], region=[US-TX], runways=[2], lat=[30.1944999694824], desc=[Austin Bergstrom International Airport]}
  {country=[US], code=[BNA], longest=[11030], city=[Nashville], elev=[599], icao=[KBNA], lon=[-86.6781997680664], type=[airport], region=[US-TN], runways=[4], lat=[36.1245002746582], desc=[Nashville International Airport]}
  {country=[US], code=[BOS], longest=[10083], city=[Boston], elev=[19], icao=[KBOS], lon=[-71.00520325], type=[airport], region=[US-MA], runways=[6], lat=[42.36429977], desc=[Boston Logan]}

Sample of 5 routes:
  {dist=811}
  {dist=214}
  {dist=945}
  {dist=576}
  {dist=546}
```

### Explore Airports and Routes

The demo then explores the graph:

```
//region Explore airports and routes
System.out.println("\n=== Exploring Airports and Routes ===");
{
    // Count airports by region (1)
    System.out.println("\nTop 5 regions by number of airports:");
    g.V().hasLabel("airport")
            .groupCount().by("region")
            .order(local).by(values, desc)
            .<Map.Entry<String, Long>>unfold().limit(5)
            .forEachRemaining(e ->
                    System.out.println("  " + e.getKey() + ": " + e.getValue() + " airports"));


    // Find airports with most routes (2)
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
```

This section:
1. Groups airports by region and counts them, showing the top 5 regions with the most airports
2. Finds the top 5 airports with the most outgoing routes

**Console Output:**
```
=== Exploring Airports and Routes ===

Top 5 regions by number of airports:
  US-CA: 7 airports
  US-TX: 6 airports
  US-FL: 5 airports
  US-NY: 4 airports
  US-AZ: 2 airports

Top 5 airports with most outgoing routes:
  Atlanta (ATL): 43 routes
  Dallas (DFW): 43 routes
  Chicago (ORD): 42 routes
  Denver (DEN): 42 routes
  Los Angeles (LAX): 40 routes
```

### Graph Algorithms

Next, the demo demonstrates some graph algorithms using Gremlin.

```
//region Graph Algorithms
System.out.println("\n=== Graph Algorithms ===");
{
    // Degree centrality - find most connected airports (1)
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

    // Find a path between two airports (2)
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

    // Find all airports reachable within 2 hops from Long Beach (3)
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
```

This section:
1. Calculates the degree centrality to find the most connected airports
2. Finds the shortest paths between Boston (BOS) and Atlanta (ATL)
3. Counts all airports reachable within 2 hops from Long Beach (LGB)

**Console Output:**
```
=== Graph Algorithms ===

Top 5 airports by degree centrality (most connections):
  Atlanta (ATL): 86 connections
  Dallas (DFW): 86 connections
  Chicago (ORD): 84 connections
  Denver (DEN): 84 connections
  Los Angeles (LAX): 80 connections

Finding shortest path between Boston (BOS) and Atlanta (ATL):
  Path (1 hops): BOS -> ATL
  Path (2 hops): BOS -> DTW -> ATL
  Path (2 hops): BOS -> PHL -> ATL
  Path (2 hops): BOS -> OAK -> ATL
  Path (2 hops): BOS -> CLE -> ATL

Count of the airports reachable within 2 hops from Long Beach (LGB):
  44 airports
```

### AQL Queries

Furthermore, we can execute native AQL queries alongside Gremlin.

```
//region AQL Queries
System.out.println("\n=== AQL Queries ===");
{
    // Find weighted k-shortest paths between two airports with an AQL query (1)
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

    // AQL traversal to find paths between two airports with constraints on edges (2)
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
```

This section:
1. Executes an AQL query to find weighted k-shortest paths between Boston and San Francisco
2. Executes an AQL query to find paths between Boston and Atlanta with a constraint that each flight must be at most 400 km

**Console Output:**
```
=== AQL Queries ===

Finding weighted k-shortest paths between Boston (BOS) and San Francisco (SFO) with AQL query:
  Path (dist: 2697):    [BOS, SLC, SFO]
  Path (dist: 2697):    [BOS, SLC, SNA, SFO]
  Path (dist: 2699):    [BOS, DTW, SFO]
  Path (dist: 2700):    [BOS, SFO]
  Path (dist: 2703):    [BOS, DTW, ORD, SFO]

Finding path between Boston (BOS) and Atlanta (ATL) with max 400 km flights with AQL query:
  Path (dist: 979):     BOS -(368)-> BWI -(255)-> RDU -(356)-> ATL
  Path (dist: 980):     BOS -(398)-> DCA -(226)-> RDU -(356)-> ATL
  Path (dist: 972):     BOS -(278)-> PHL -(338)-> RDU -(356)-> ATL
  Path (dist: 1153):    BOS -(368)-> BWI -(91)-> PHL -(338)-> RDU -(356)-> ATL
  Path (dist: 1212):    BOS -(398)-> DCA -(120)-> PHL -(338)-> RDU -(356)-> ATL
```

### Close the Graph

Finally, we can close the graph connection:

```
graph.close();
```
