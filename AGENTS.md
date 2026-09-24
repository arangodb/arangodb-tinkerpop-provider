# ArangoDB TinkerPop provider: agent instructions

This repository implements TinkerPop's `Graph` API over ArangoDB, with simple and
complex graph models, AQL filter pushdown, and Gremlin Console/Server integration.

For maintenance, features, bug fixes, refactoring, dependencies, or tests, use the
[tinkerpop-provider-development skill](.agents/skills/tinkerpop-provider-development/SKILL.md).
Load only the references relevant to the change. Agents without skill discovery
can read the same files directly.

## Compatibility and scope

- Preserve public APIs, configuration defaults, element identity, persisted data,
  and Gremlin semantics unless the task explicitly changes the contract. Check
  both graph models when changing shared code.
- The root POM uses Java 8 source/target settings, but CI runs JDK 11 and 21. Keep
  that language target and JDK 11 compatibility; do not infer Java 8 runtime
  support or permission to use JDK 21 APIs. Separate consumer POMs have their own
  targets.
- Match neighboring code and Apache license headers. Avoid unrelated formatting,
  dependency upgrades, release/version changes, or edits to released changelog
  entries. Edit source inputs, not `target/` or `.flattened-pom.xml`. For
  user-visible changes, update affected Javadocs and examples and add a
  `CHANGELOG.md` entry under `[Unreleased]`.

## Validation

Use `mvn` (no wrapper). `mvn test-compile` compiles production and test sources
without running tests or requiring a database. `mvn test` is **not** a
database-free unit-test command: it selects the simple graph test package by
default. Use the skill's [testing guide](.agents/skills/tinkerpop-provider-development/references/testing.md)
for suite selection and the matching CircleCI job.

Database fixtures delete graph/collection data. Use disposable infrastructure;
do not run suites or smoke tests concurrently against shared fixtures. Do not use
release, deployment, or remote publishing commands for local validation.

Treat source, POMs, and `.circleci/config.yml` as evidence of current behavior. If
this guidance disagrees, investigate and correct the drift rather than changing
code to match stale documentation. Report the changed contract, checks actually
run and their results, and relevant untested variants or environment blockers.
A skipped, unselected, or stale test result is not a passing regression test.
