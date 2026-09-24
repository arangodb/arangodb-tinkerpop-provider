# Build and test guide

Contents: [environment](#environment), [test selection](#test-selection),
[test placement](#test-placement), [CircleCI test jobs](#circleci-test-jobs),
[consumer jobs](#consumer-jobs), [analysis](#analysis), [reporting](#reporting).

`.circleci/config.yml` is the source of truth for job parameters and workflow
conditions; this guide maps its jobs to local commands.

## Environment

Run commands from the repository root with `mvn` (Maven 3.6.3+). CI uses JDK 21
for everything and additionally JDK 11 for the `test-jdk` workflow; the enforcer
rejects JDK 23+. When switching JDKs, run `mvn clean` first. The independent
consumers target newer releases (`demo/`: 21, `test-plugin/java-client/`: 17).

`mvn test-compile` needs no database. Every other test run needs a disposable
ArangoDB started by `docker/start_db.sh`, configured through `DOCKER_IMAGE`
(default `docker.io/arangodb/enterprise:latest`), `STARTER_MODE` (`single` |
`cluster`), `SSL` (`true` | `false`) and, when the image requires it,
`ARANGO_LICENSE_KEY`. The script needs Docker access (it mounts the Docker
socket), creates fixed-name resources (network `arangodb` on `172.28.0.0/16`,
containers `adb` and `arangodb-data`, plus the server containers spawned by the
starter) and sets the `root` password to `test`.

One fixture exists at a time: before switching image, topology or TLS, remove the
resources created by the previous run of the script; never use broad Docker
cleanup or a shared/production database. Tests connect to `172.28.0.1:8529`; the
fixed gateway may require extra setup on non-Linux Docker hosts. Tests based on
`AbstractTest` or `TestGraphProvider` accept `-Darango.endpoints=host:port`;
YAML/properties fixtures, the demo, and the smoke-test configurations do not.

## Test selection

Surefire (`mvn test`) includes `com.arangodb.tinkerpop.gremlin.${test.graph.type}.**`,
default `simple`. `test.graph.type` (`simple`, `complex`, `ssl`) selects a **test
package**; the graph model is set by each package's providers and fixtures.
`-Dtest=...` replaces the package include:

```sh
mvn test -Dtest=SimpleArangoDBSuiteTest,ComplexArangoDBSuiteTest
mvn test -Dtest=ArangoDBGraphConfigTest
```

Do not disable Surefire's failure on unmatched `-Dtest` patterns. Check fresh
`target/surefire-reports/` to confirm the expected methods ran rather than being
skipped by an assumption. There are no Failsafe integration tests.

## Test placement

JUnit 4 with the neighboring fixture/assertion style (including AssertJ). Paths
start at `src/test/java/com/arangodb/tinkerpop/gremlin/`.

| Coverage | Placement / entry point |
| --- | --- |
| Provider behavior: IDs, persistence, data types, filter pushdown, native AQL | Leaf test under `arangodb/`, registered in `arangodb/simple/SimpleArangoDBSuite` and/or `arangodb/complex/ComplexArangoDBSuite`; run via `simple/SimpleArangoDBSuiteTest` / `complex/ComplexArangoDBSuiteTest`. |
| Graph creation, data definition, configuration, variables | Class under `simple/` or `complex/` extending `AbstractTest` (which connects to the database in `@Before`). |
| Upstream structure / process contracts | `*StructureStandardSuiteTest` / `*ProcessStandardSuiteTest` in `simple/` and `complex/`. |
| Adapted upstream tests | `custom/CustomStandardSuite`, run via `simple/SimpleCustomStandardSuiteTest`. |
| TLS connection | `arangodb/simple/SslConnectionTest`, run via `ssl/SslSuiteTest`. |

`arangodb/` is outside every package include: a leaf there runs only if it is
registered in a suite, and its `AbstractGremlinTest` setup requires the suite's
`@GraphProviderClass`, so select the suite wrapper, not the leaf. Register shared
behavior in both suites. `SimpleArangoDBSuiteTest` uses a custom label field
(`CustomLabelSimpleGraphProvider`); the complex standard suites use
`ComplexGraphWithoutIdPrefixProvider`, which adapts upstream test IDs, while
`ComplexArangoDBSuiteTest` checks production-style IDs. A test that is not
reachable from the CI package includes is not regression coverage, even if it
passes with `-Dtest`.

## CircleCI test jobs

The `test` job runs `start_db.sh` with its image/topology/TLS parameters, then
`mvn dependency:tree` and `mvn test -Dtest.graph.type=<graphType> <args>`.

| Workflow / job | Image | Topology / TLS | JDK | `graphType` | Extra args |
| --- | --- | --- | --- | --- | --- |
| `test-adb-version` / `test-adb-single-*` | `docker.io/arangodb/enterprise:3.12`, `docker.io/arangodb/core-preview:4-nightly` | single | 21 | `simple`, `complex` | – |
| `test-adb-version` / `test-adb-cluster-*` | same two images | cluster | 21 | `simple`, `complex` | `-Dtest.skipProcessStandardSuite` |
| `test-adb-version` / `test-ssl` | `enterprise:latest` | single, TLS | 21 | `ssl` | `-Dtest.ssl` |
| `test-jdk` / `test-jdk=*` (only `main` and `v*` tags) | `enterprise:latest` | single | 11, 21 | `simple`, `complex` | – |
| `test-adb-topology` / `test-single`, `test-cluster` | pipeline parameter `docker-img` | single, cluster | 21 | `simple` | cluster: `-Dtest.skipProcessStandardSuite` |

A nonempty `docker-img` pipeline parameter runs only `test-adb-topology` (plus the
tag-filtered `deploy`); otherwise all other workflows run. Reproduce the
dimensions the change affects, not the whole matrix. Examples:

```sh
# single server (version or JDK matrix)
DOCKER_IMAGE=docker.io/arangodb/enterprise:3.12 STARTER_MODE=single SSL=false ./docker/start_db.sh
mvn test -Dtest.graph.type=simple
mvn test -Dtest.graph.type=complex

# cluster, on a fresh fixture
DOCKER_IMAGE=docker.io/arangodb/enterprise:3.12 STARTER_MODE=cluster SSL=false ./docker/start_db.sh
mvn test -Dtest.graph.type=simple -Dtest.skipProcessStandardSuite
mvn test -Dtest.graph.type=complex -Dtest.skipProcessStandardSuite

# TLS, on a fresh fixture
DOCKER_IMAGE=docker.io/arangodb/enterprise:latest STARTER_MODE=single SSL=true ./docker/start_db.sh
mvn test -Dtest.graph.type=ssl -Dtest.ssl
```

`-Dtest.skipProcessStandardSuite` skips only the process standard suites and is
used only for clusters; do not add it to single-server runs. Without `-Dtest.ssl`
the TLS suite is skipped by an assumption. The TLS job covers the connection
only, not the simple/complex suites over TLS.

## Consumer jobs

`test-demo`, `test-console` and `test-plugin` use JDK 21 and the default fixture
(`enterprise:latest`, single, no TLS), install the provider, then run one command:

```sh
DOCKER_IMAGE=docker.io/arangodb/enterprise:latest STARTER_MODE=single SSL=false ./docker/start_db.sh
mvn clean install -DskipTests
```

| Job | Command |
| --- | --- |
| `test-demo` | `(cd demo && mvn compile exec:java -Dexec.mainClass="org.example.Main")` |
| `test-console` | `./test-console/test.sh` |
| `test-plugin` | `./test-plugin/test.sh` |

They cover different artifacts: the Console test loads the first
`target/arangodb-tinkerpop-provider-*-standalone.jar`; the plugin test copies the
locally installed Maven artifact into a Gremlin Server container, installs a
hard-coded provider version there, queries it from Gremlin Console and then from
`test-plugin/java-client`. Both scripts recreate fixed-name containers, so run
them sequentially. `clean` avoids a stale standalone JAR being picked up.

## Analysis

The `sonar` job runs, on the default fixture:

```sh
mvn verify -Dtest.graph.type=simple org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=arangodb_arangodb-tinkerpop-provider -Pstatic-code-analysis
```

Locally, omit the Sonar goal and properties (they publish remotely):
`mvn verify -Dtest.graph.type=simple -Pstatic-code-analysis`. The profile makes
SpotBugs fail the build on findings (exclusions in `spotbugs/spotbugs-exclude.xml`);
JaCoCo and the Apache RAT license check run in every build. Never run the
`deploy` job, `mvn deploy`, or the `release` profile for validation.

## Reporting

For documentation-only changes, verify referenced paths and commands against the
sources, POMs and CI, and run `git diff --check`; no database is needed.
Otherwise, report which suites ran with which image, topology, TLS and JDK, the
skipped tests, and the relevant CI jobs not reproduced. Distinguish
infrastructure problems from assertion failures, and do not claim CI parity from
a focused or compile-only run.
