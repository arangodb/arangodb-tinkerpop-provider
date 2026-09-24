# Architecture and ownership

Contents: [repository map](#repository-map), [lifecycle](#opening-and-mutating-a-graph),
[identity and storage](#identity-and-stored-representation),
[traversals](#traversal-execution), [artifacts](#serialization-and-artifact-boundaries).

Paths are repository-relative unless linked. Java paths below are relative to
`src/main/java/com/arangodb/tinkerpop/gremlin/`.

## Repository map

The root `pom.xml` builds a single provider module, not a multi-module reactor. `demo/`
and `test-plugin/java-client/` are independent Maven consumers. `test-console/`
and `test-plugin/` exercise Dockerized TinkerPop distributions, not root Surefire
suites. See the testing guide for their separate CI jobs.

| Area | Responsibility |
| --- | --- |
| `structure/ArangoDBGraph.java`, `ArangoDBGraphConfig.java` | Graph lifecycle, configuration, schema checks, TinkerPop entrypoints, native AQL entrypoint. |
| `structure/ArangoDBVertex.java`, `ArangoDBEdge.java`, property and element classes | Gremlin identity, mutation, property access, adjacency, removed-element behavior. |
| `structure/ArangoDBGraphFeatures.java` | Advertised TinkerPop capabilities; also influences conformance-test selection. |
| `client/ArangoDBGraphClient.java`, `ArangoDBQueryBuilder.java` | Java-driver calls, AQL construction, CRUD, graph variables, exception translation. |
| `persistence/`, `persistence/simple/`, `persistence/complex/` | Element IDs, data objects, and the two identity models. |
| `persistence/serde/` | Jackson document mapping, including IDs, vertex meta-properties, edges, and graph variables. |
| `process/traversal/`, `process/filter/`, `process/value/` | Provider strategy/steps, predicate translation, AQL literals, pushdown support levels. |
| `utils/` | Configuration builder, value validation/normalization, reserved fields, version checks, native AQL result decoding. |
| `jsr223/ArangoDBGremlinPlugin.java` | Gremlin plugin name and imported classes. |

## Opening and mutating a graph

`ArangoDBGraph.open(Configuration)` constructs `ArangoDBGraphConfig`, selects an
`ElementIdFactory`, and creates `ArangoDBGraphClient`. The client selects a mapper
for the driver's protocol/content type, registers `SerdeModule`, and constructs
its own Java driver. The graph checks the database and graph definition, creating
missing definitions only when `enableDataDefinition` allows it.

Opening is not read-only: the graph also ensures `TINKERPOP-GRAPH-VARIABLES`,
creates/loads the graph's variables document, and checks/updates its provider
version, refusing a graph persisted by a newer provider (`ArangoDBUtil.checkVersion`).
Disabling data definition does not suppress those metadata writes.
`ArangoDBGraph.close()` shuts down its client's driver.

Element mutations persist immediately through the client; vertex/edge updates
use replacement operations rather than a deferred unit of work. Property and
meta-property changes write through their owning persistent element. Vertex
removal traverses/removes incident edges before deleting the vertex. The provider
does not implement TinkerPop transactions or `GraphComputer`.

## Identity and stored representation

| | SIMPLE | COMPLEX |
| --- | --- | --- |
| Collections | One vertex and one edge collection; defaults are `vertex` and `edge`. | Multiple collections from edge definitions and orphan collections. |
| Public vertex/edge ID | Document key, without `/`. | `collection/key`. |
| Label | Stored in the configurable `graph.labelField` (default `_label`). | Collection name, derived from the ID. |
| Label filtering | Document label field. | Collection selection. |

Use the implementations in `persistence/simple/` and `persistence/complex/` as
the identity reference. Complex IDs do **not** add a graph-name prefix; the
`GraphType.COMPLEX` Javadoc still describes an older format. The
`ComplexTestGraphWithoutIdPrefix` test adapter supplies collection prefixes for
upstream test IDs; it is not a production ID rule.

`ElementId` distinguishes the public ID from its database handle. In particular,
a simple element's `id()` is not a document handle; `graph.elementId(element)`
provides the representation usable in AQL bind parameters.

Vertex properties are document fields; their meta-properties are stored under
`_meta`. Edge data additionally carries `_from` and `_to`. `Fields` and
`ArangoDBGraphConfig.isReservedField()` separate storage metadata and the configured
label field from user properties. Vertex properties support only `single`
cardinality (no multi-properties), but do support meta-properties. Graph variables
have a separate data type and serializer and retain the provider version.

## Traversal execution

```text
GraphTraversalSource -> GraphStep -> ArangoStepStrategy -> ArangoStep
  -> collection/ID selection + ArangoFilter -> ArangoDBGraphClient -> AQL/driver
  -> VertexData/EdgeData -> ArangoDBVertex/ArangoDBEdge -> HasContainer.testAll
```

A static initializer in `ArangoDBGraph` registers `ArangoStepStrategy` in
`TraversalStrategies.GlobalCache` for the graph class. The strategy replaces
`GraphStep` and folds subsequent `HasStep`s, traversing
`NoOpBarrierStep`s and preserving step labels. It is not a general Gremlin-to-AQL
compiler. Adjacency uses `ArangoDBVertex` and the client's one-hop AQL builders.

`ArangoStep` normalizes predicate values, maps ID/label keys by graph type, and
selects collections. For implemented predicates, `FilterSupport` describes exact
(`FULL`), superset (`PARTIAL`), or unavailable (`NONE`) pushdown. Results are still
checked with all original `HasContainer`s. Unrecognized predicates currently throw
`UnsupportedOperationException` in `ArangoFilter.of()`; not every unsupported
predicate silently falls back.

`ArangoDBGraph.aql(...)` instead inserts `AQLStartStep`. `AqlDeserializer` recognizes
edge documents before vertex documents by their system fields, recursively maps
arrays/objects, and returns scalar values otherwise. Both paths ultimately use
Java-driver cursor streams; consider early termination and resource ownership
when changing iteration.

## Serialization and artifact boundaries

`ArangoDBGraphClient` uses the unshaded Jackson types from the serializer modules
for its document mapper. `ArangoDBUtil.MAPPER` uses the driver's shaded Jackson
namespace for utilities such as AQL value rendering. These are distinct type
systems, not interchangeable imports.

The root POM uses the shaded driver and JSON serializer, with the VelocyPack
serializer in `provided` scope. `src/assembly/standalone.xml` packages compile and
provided dependencies into `*-standalone.jar` and merges service descriptors.
Plugin discovery uses
`src/main/resources/META-INF/services/org.apache.tinkerpop.gremlin.jsr223.GremlinPlugin`.

The Maven replacer generates `PackageVersion.java` from `PackageVersion.java.in`
and the POM version. Generated sources and the flattened publication POM are build
outputs, not independent editing points.
