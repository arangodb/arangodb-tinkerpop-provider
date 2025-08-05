![ArangoDB-Logo](https://docs.arangodb.com/assets/arangodb_logo_2016_inverted.png)

# arangodb-tinkerpop-provider

An implementation of
the [Apache TinkerPop OLTP Provider](https://tinkerpop.apache.org/docs/3.7.3/dev/provider) API for ArangoDB.


## Compatibility

This Provider supports:

* Apache TinkerPop 3.7
* ArangoDB 3.12+
* ArangoDB Java Driver 7.22+
* Java 8+


## Maven

To add the provider to your project via Maven, you need to add the following dependency (check the latest version 
[here](https://search.maven.org/artifact/com.arangodb/arangodb-tinkerpop-provider)):

```XML

<dependencies>
    <dependency>
        <groupId>com.arangodb</groupId>
        <artifactId>arangodb-tinkerpop-provider</artifactId>
        <version>x.y.z</version>
    </dependency>
</dependencies>
```


## Configuration

The graph can be created using the methods `org.apache.tinkerpop.gremlin.structure.util.GraphFactory.open(...)` (see 
related [javadoc](https://tinkerpop.apache.org/javadocs/3.7.3/full/org/apache/tinkerpop/gremlin/structure/util/GraphFactory.html)).
These accept a configuration file (e.g. YAML or properties file), a java Map or Apache Commons Configuration object.

The property `gremlin.graph` must be set to: `com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph`.

The graph configuration properties related to `ArangoDBGraph` are prefixed with `gremlin.arangodb.conf.graph` and are:
- `gremlin.arangodb.conf.graph.db`: ArangoDB database name, (default: `_system`)
- `gremlin.arangodb.conf.graph.name`: ArangoDB graph name, (default: `tinkerpop`)
- `gremlin.arangodb.conf.graph.enableDataDefinition`: flag to allow data definition changes (default: `false`)
- `gremlin.arangodb.conf.graph.type`: graph type: `SIMPLE` or `COMPLEX`, (default: `SIMPLE`)
- `gremlin.arangodb.conf.graph.orphanCollections`: list of orphan collections names
- `gremlin.arangodb.conf.graph.edgeDefinitions`: list of edge definitions in the format: `edge1:[col1,col2]->[col3,col4]`

The driver configuration properties are prefixed with `gremlin.arangodb.conf.driver`.
All properties keys from `com.arangodb.config.ArangoConfigProperties` are supported as driver properties, see related 
[documentation](https://docs.arangodb.com/stable/develop/drivers/java/reference-version-7/driver-setup/#config-file-properties).

Here is an example of the configuration with a YAML file:

```yaml
gremlin:
  graph: "com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph"
  arangodb:
    conf:
      graph:
        db: "testDb"
        name: "myFirstGraph"
        enableDataDefinition: true
        type: COMPLEX
        orphanCollections: [ "x", "y", "z" ]
        edgeDefinitions:
          - "e1:[a]->[b]"
          - "e2:[a,b]->[c,d]"
      driver:
        user: "root"
        password: "test"
        hosts:
          - "172.28.0.1:8529"
          - "172.28.0.1:8539"
          - "172.28.0.1:8549"
```

which can be loaded in this way:
```java
    ArangoDBGraph graph = (ArangoDBGraph) GraphFactory.open("<path_to_yaml_file>");
```

Alternatively, the graph configuration can be created programmatically using the help of the configuration builder 
`com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder`:

```java
Configuration conf = new ArangoDBConfigurationBuilder()
        .hosts("172.28.0.1:8529")
        .user("root")
        .password("test")
        .database("testDb")
        .enableDataDefinition(true)
        .build();
ArangoDBGraph graph = (ArangoDBGraph) GraphFactory.open(conf);
```

Additional configuration examples can be found [here](./src/test/java/example).

### SSL Configuration

To use TLS-secured connections to ArangoDB, set `gremlin.arangodb.conf.driver.useSsl` to `true` and optionally configure 
the other driver-related properties, see related
[documentation](https://docs.arangodb.com/stable/develop/drivers/java/reference-version-7/driver-setup/#config-file-properties. 

For example:

```yaml
gremlin:
  graph: "com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph"
  arangodb:
    conf:
      driver:
        hosts:
          - "172.28.0.1:8529"
        useSsl: true
        verifyHost: false
        sslCertValue: "MIIDezCCAmOgAwIBAgIEeDCzXzANBgkqhkiG9w0BAQsFADBuMRAwDgYDVQQGEwdVbmtub3duMRAwDgYDVQQIEwdVbmtub3duMRAwDgYDVQQHEwdVbmtub3duMRAwDgYDVQQKEwdVbmtub3duMRAwDgYDVQQLEwdVbmtub3duMRIwEAYDVQQDEwlsb2NhbGhvc3QwHhcNMjAxMTAxMTg1MTE5WhcNMzAxMDMwMTg1MTE5WjBuMRAwDgYDVQQGEwdVbmtub3duMRAwDgYDVQQIEwdVbmtub3duMRAwDgYDVQQHEwdVbmtub3duMRAwDgYDVQQKEwdVbmtub3duMRAwDgYDVQQLEwdVbmtub3duMRIwEAYDVQQDEwlsb2NhbGhvc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC1WiDnd4+uCmMG539ZNZB8NwI0RZF3sUSQGPx3lkqaFTZVEzMZL76HYvdc9Qg7difyKyQ09RLSpMALX9euSseD7bZGnfQH52BnKcT09eQ3wh7aVQ5sN2omygdHLC7X9usntxAfv7NzmvdogNXoJQyY/hSZff7RIqWH8NnAUKkjqOe6Bf5LDbxHKESmrFBxOCOnhcpvZWetwpiRdJVPwUn5P82CAZzfiBfmBZnB7D0l+/6Cv4jMuH26uAIcixnVekBQzl1RgwczuiZf2MGO64vDMMJJWE9ClZF1uQuQrwXF6qwhuP1Hnkii6wNbTtPWlGSkqeutr004+Hzbf8KnRY4PAgMBAAGjITAfMB0GA1UdDgQWBBTBrv9Awynt3C5IbaCNyOW5v4DNkTANBgkqhkiG9w0BAQsFAAOCAQEAIm9rPvDkYpmzpSIhR3VXG9Y71gxRDrqkEeLsMoEyqGnw/zx1bDCNeGg2PncLlW6zTIipEBooixIE9U7KxHgZxBy0Et6EEWvIUmnr6F4F+dbTD050GHlcZ7eOeqYTPYeQC502G1Fo4tdNi4lDP9L9XZpf7Q1QimRH2qaLS03ZFZa2tY7ah/RQqZL8Dkxx8/zc25sgTHVpxoK853glBVBs/ENMiyGJWmAXQayewY3EPt/9wGwV4KmU3dPDleQeXSUGPUISeQxFjy+jCw21pYviWVJTNBA9l5ny3GhEmcnOT/gQHCvVRLyGLMbaMZ4JrPwb+aAtBgrgeiK4xeSMMvrbhw=="
```

If no `sslCertValue` configuration parameter is provided, then the default SSL context will be used.
In such cases, the truststore can be specified, if needed, using system properties `javax.net.ssl.trustStore` and 
`javax.net.ssl.trustStorePassword`.

Additional configuration examples can be found [here](./src/test/java/example).

### enableDataDefinition

When a graph is instantiated, the already existing data definitions in ArangoDB are compared with the structure expected
by the configuration of `gremlin.arangodb.conf.graph`. 
This checks whether:
- the database exists
- the graph exists
- the graph structure has the same edge definitions and orphan collections

In case of mismatch, an error is thrown and the graph will not be instantiated.
In case data definitions in ArangoDB are not present, they can be automatically created by configuring 
`gremlin.arangodb.conf.graph.enableDataDefinition` flag to `true`. This would allow creating a new database (if not 
already existing) and a new graph (if not already existing). Existing graphs are never modified.

Collection names (vertex and edge collections) will be prefixed with the graph name, if they are not already.


## Graph Type

The graph type can be configured with the property `gremlin.arangodb.conf.graph.type` and can be `SIMPLE` or `COMPLEX`.

### SIMPLE

`SIMPLE` graph type is a graph definition that allows only 1 vertex collection and 1 edge collection.
Collection names are prefixed with the graph name, e.g. `<graphName>_vCol`.
By default, the vertex collection name is `<graphName>_vertex` and the edge collection name is `<graphName>_edge`.

Element ids have no format constrains.

Any label can be used at runtime.

Here is an example of the configuration for a simple graph:

```yaml
gremlin:
  graph: "com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph"
  arangodb:
    conf:
      graph:
        db: "db"
        name: "graph"
        type: SIMPLE
        edgeDefinitions:
          - "e:[v]->[v]"
```

### COMPLEX

`COMPLEX` graph type is a graph definition that allows multiple vertex collections and multiple edge collections.

Element ids are strings with the format constraint: `<graph>_<label>/<key>`, where:
- `<graph>` is the graph name
- `<label>` is the element label
- `<key>` is the db document key

At runtime, only labels corresponding graph collections can be used i.e., `<graph>_<label>` is a vertex or edge 
collection of the graph. 

Graph name, label and key must not contain `_`.

Here is an example of the configuration for a simple graph:

```yaml
gremlin:
  graph: "com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph"
  arangodb:
    conf:
      graph:
        db: "db"
        name: "graph"
        type: COMPLEX
        edgeDefinitions:
          - "e1:[v1,v2]->[v3]"
          - "e2:[v3]->[v5,v1]"
```


## Naming constraints

- element ids must be strings
- `_` character is used as separator for collection names (e.g. `myGraph_myCol`). Therefore, it is not allowed using `_`
  in:
  - graph name (`gremlin.arangodb.conf.graph.type`), 
  - labels
  - element ids, in `SIMPLE` graph types
  - vertex and edge keys, in `COMPLEX` graph types


## Persistent structure
TODO


## Supported Features

This library supports the following features:

```
> GraphFeatures
>-- Computer: false
>-- Persistence: true
>-- ConcurrentAccess: true
>-- Transactions: false
>-- ThreadedTransactions: false
>-- IoRead: true
>-- IoWrite: true
>-- OrderabilitySemantics: false
>-- ServiceCall: false
> VariableFeatures
>-- Variables: true
>-- BooleanValues: true
>-- ByteValues: false
>-- DoubleValues: true
>-- FloatValues: false
>-- IntegerValues: true
>-- LongValues: true
>-- MapValues: false
>-- MixedListValues: false
>-- SerializableValues: false
>-- StringValues: true
>-- UniformListValues: false
>-- BooleanArrayValues: true
>-- ByteArrayValues: false
>-- DoubleArrayValues: true
>-- FloatArrayValues: false
>-- IntegerArrayValues: true
>-- LongArrayValues: true
>-- StringArrayValues: true
> VertexFeatures
>-- DuplicateMultiProperties: false
>-- AddVertices: true
>-- RemoveVertices: true
>-- MultiProperties: false
>-- MetaProperties: true
>-- Upsert: false
>-- NullPropertyValues: true
>-- AddProperty: true
>-- RemoveProperty: true
>-- UserSuppliedIds: true
>-- NumericIds: false
>-- StringIds: true
>-- UuidIds: false
>-- CustomIds: false
>-- AnyIds: false
> VertexPropertyFeatures
>-- NullPropertyValues: true
>-- RemoveProperty: true
>-- UserSuppliedIds: false
>-- NumericIds: true
>-- StringIds: true
>-- UuidIds: true
>-- CustomIds: true
>-- AnyIds: false
>-- Properties: true
>-- BooleanValues: true
>-- ByteValues: false
>-- DoubleValues: true
>-- FloatValues: false
>-- IntegerValues: true
>-- LongValues: true
>-- MapValues: false
>-- MixedListValues: false
>-- SerializableValues: false
>-- StringValues: true
>-- UniformListValues: false
>-- BooleanArrayValues: true
>-- ByteArrayValues: false
>-- DoubleArrayValues: true
>-- FloatArrayValues: false
>-- IntegerArrayValues: true
>-- LongArrayValues: true
>-- StringArrayValues: true
> EdgeFeatures
>-- Upsert: false
>-- AddEdges: true
>-- RemoveEdges: true
>-- NullPropertyValues: true
>-- AddProperty: true
>-- RemoveProperty: true
>-- UserSuppliedIds: true
>-- NumericIds: false
>-- StringIds: true
>-- UuidIds: false
>-- CustomIds: false
>-- AnyIds: false
> EdgePropertyFeatures
>-- Properties: true
>-- BooleanValues: true
>-- ByteValues: false
>-- DoubleValues: true
>-- FloatValues: false
>-- IntegerValues: true
>-- LongValues: true
>-- MapValues: false
>-- MixedListValues: false
>-- SerializableValues: false
>-- StringValues: true
>-- UniformListValues: false
>-- BooleanArrayValues: true
>-- ByteArrayValues: false
>-- DoubleArrayValues: true
>-- FloatArrayValues: false
>-- IntegerArrayValues: true
>-- LongArrayValues: true
>-- StringArrayValues: true
```


## Usage

The [demo](./demo) project contains usage examples of this library.
For additional examples please check the [Gremlin tutorial](https://tinkerpop.apache.org/docs/3.7.3/tutorials/getting-started/).


## Element Ids

Given a Gremlin element, the corresponding database id (`_id` field in ArangoDB documents) can be computed using
`com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph.elementId(Element)`, for example:

```java
    Vertex v = graph.addVertex("name", "marko");
    String id = graph.elementId(v);
```


## AQL queries

AQL queries can be executed via `com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph.aql()`, for example:
```java
    Vertex v = graph.addVertex("name", "marko");
    String id = graph.elementId(v);
    List<Vertex> result = graph
            .<Vertex>aql("RETURN DOCUMENT(@id)", Map.of("id", id))
            .toList();
```


## Current limitations

- This library implements Online Transactional Processing Graph Systems (OLTP) API only, the Online Analytics Processing
  Graph Systems (OLAP) API is currently not implemented.
- This library implements Structure API only, the Process API is currently not implemented. 
  To improve query performance, it is currently recommended using [AQL queries](#aql-queries).


## Acknowledgments

This repository is based on and extends the original work of the
[arangodb-community/arangodb-tinkerpop-provider](https://github.com/arangodb-community/arangodb-tinkerpop-provider)
project.

We gratefully acknowledge the efforts of [Horacio Hoyos Rodriguez](https://github.com/arcanefoam) and other contributors
of the community repository, see [AUTHORS.md](./AUTHORS.md).
