# Change guide

Read the sections touching the change. Java paths below are relative to
`src/main/java/com/arangodb/tinkerpop/gremlin/`.

## Graph model and persistence

Keep ID creation/parsing in `ElementIdFactory` and its model-specific subclasses.
Check generated and supplied IDs, label/collection agreement, graph collection
membership, and lookup by ID or element where affected. Do not generalize the
upstream test adapters' relaxed ID input into production behavior.

For property or type changes, follow `ArangoDBElement`, `ArangoDBVertex`,
`ArangoDBEdge`, `ArangoDBVertexProperty`, the data objects, and `persistence/serde/`.
Coordinate validation/normalization in `ArangoDBUtil`, reserved-field handling in
`Fields`/configuration, filter values, and `ArangoDBGraphFeatures`. Avoid hard-coded
`_label`: the provider-specific simple suite deliberately uses a custom label field.

Treat document shape as a compatibility boundary. Check read/write round trips,
meta-properties under `_meta`, edge endpoints, and relevant null/missing-value or
numeric/array behavior. Verify persistence by rereading data, not just observing
the mutated object. Replacement writes and local element state can interact with
stale instances; do not silently add transaction or concurrency guarantees.

Keep feature declarations truthful. Supporting a capability requires behavior and
coverage; disabling one or adding `Graph.OptOut` is not a fix for a regression.
Preserve justified upstream test adaptations and review them when upgrading
TinkerPop, rather than assuming every exclusion is permanent.

## Traversal and AQL

Preserve this correctness condition: pushed-down filters must return **at least**
every element that the original Gremlin predicate would accept. The final
`HasContainer.testAll` check can remove false positives but cannot recover rows
already discarded by AQL. A `FilterSupport` value or a composition truth-table
comment is not proof that this condition holds.

When extending `ArangoFilter` or `Value`, test boolean composition as well as the
new primitive predicate. In particular, dropping an unsupported `OR` branch or
negating a partial filter can lose valid results. Retain client-side checks unless
the change establishes full semantic equivalence, including missing properties,
nulls, numeric comparisons, arrays/maps, and applicable text/regex behavior.

Keep `T.id`, `T.label`, custom label fields, and collection pruning consistent with
both graph models. Test explicit IDs and empty/nonmatching collection selections
where affected. For strategy changes, also check step labels, ID folding, and
barriers; changing the query text alone does not validate traversal semantics.

Compare optimized traversals with equivalent traversals from
`graph.traversal().withoutStrategies(ArangoStepStrategy.class)` on the same fixture,
using assertions that preserve relevant multiplicity and ordering. Also assert the
intended result independently so two paths sharing a defect do not validate each
other. Keep AQL-generation assertions when they prove the intended pushdown.

Use bind parameters for data where possible and the existing `Value.toAql()` path
when rendering literals. Identifier quoting and value encoding are different:
check graph/collection/property names and escaping at the affected construction
site instead of assuming surrounding backticks make arbitrary input safe.
For native `aql(...)` changes, cover scalar/nested results and document-to-element
conversion, not just a vertex-only query.

## Configuration and lifecycle

Keep `ArangoDBConfigurationBuilder`, `ArangoDBGraphConfig`, defaults, and the
file/map examples aligned. Graph options live under `gremlin.arangodb.conf.graph`;
driver options under `gremlin.arangodb.conf.driver`. Distinguish definition
creation from opening/validating existing graphs and graph-variable writes.

For serializer or driver setup changes, retain the protocol-selected mapper and
`SerdeModule` registrations. Check the intended JSON/VelocyPack consumer classpath;
a provided dependency present during tests is not proof it is available to an
application. Do not replace unshaded Jackson types with shaded ones mechanically.

For client/iterator changes, inspect driver shutdown, cursor stream ownership,
exception paths, and partial consumption. Preserve the existing mapping of
interruption and duplicate-key errors and the deliberate handling of missing
documents during deletion. A refactor should not broaden ignored errors.

## Dependencies and packaging

Read the root and affected consumer POMs. Retain dependency convergence, explicit
scopes, and bytecode/plugin enforcement instead of disabling checks to make an
upgrade pass. Keep the three driver-related artifacts on the shared version
property. For a TinkerPop upgrade, check the parent, Gremlin dependencies, test
adaptations, Docker image tags in both smoke scripts, and the Java client's POM.

For plugin imports, service metadata, dependency scope, or assembly changes,
validate the packaged artifact with the matching Console/Server jobs. Inspect
`src/assembly/standalone.xml` and the Gremlin plugin service descriptor; a passing
source-classpath test does not establish discovery from the distributed JAR.

Only for an intentional provider-version change, align the consumer dependency
in `demo/pom.xml`, the install coordinate in `test-plugin/entrypoint.sh`, and other
affected version references. Generate `PackageVersion` via Maven, not by editing
`target/`. Recheck the persisted graph-version guard when changing version behavior.
