---
name: tinkerpop-provider-development
description: >-
  Maintain and extend the arangodb-tinkerpop-provider repository. Use for features,
  bug fixes, refactoring, Gremlin/AQL traversal behavior, graph models and
  persistence, configuration, dependency upgrades, packaging, and adding or
  running tests against the CircleCI jobs. Not a guide to using the provider in
  an application.
---

# TinkerPop provider development

Apply the repository's [AGENTS.md](../../../AGENTS.md). Read references selectively;
do not load the whole source tree or every guide to start a small change.

## Locate the contract

Trace the affected operation from the TinkerPop-facing API through the graph
model, client/query construction, and persistence boundary. Establish the expected
behavior from the task, relevant TinkerPop contract, and existing tests. Distinguish
an implementation defect from an intentionally unsupported capability.

| Task | Read as needed |
| --- | --- |
| Find ownership, graph-model differences, or execution paths | [Architecture](references/architecture.md) |
| Change identity, properties, storage, or advertised features | [Graph model and persistence](references/changes.md#graph-model-and-persistence) |
| Change traversal optimization, predicates, or AQL | [Traversal and AQL](references/changes.md#traversal-and-aql) |
| Change configuration, serialization setup, or lifecycle | [Configuration and lifecycle](references/changes.md#configuration-and-lifecycle) |
| Upgrade dependencies or change plugin/artifact packaging | [Dependencies and packaging](references/changes.md#dependencies-and-packaging) |
| Add a regression, select suites, or reproduce a CI job | [Testing](references/testing.md) |

## Implement and validate

Keep the fix at the layer owning the behavior, checking both simple and complex
paths where shared code is involved. For a bug, add a reproducer and demonstrate
failure before the fix when feasible. For a refactor, retain observable behavior,
including persisted representation and exception behavior.

Use the testing guide to register the regression in an executed package or suite.
Start with focused coverage, then choose the relevant CI dimensions and packaged
consumers; do not run the entire matrix for every change. Review the final diff
for unintended contract changes and report validation as required by AGENTS.md.
