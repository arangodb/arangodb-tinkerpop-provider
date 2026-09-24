# dev-README

## Working on the provider

Start with [AGENTS.md](AGENTS.md) for repository-wide constraints and the
[development skill](.agents/skills/tinkerpop-provider-development/SKILL.md) for
on-demand [architecture](.agents/skills/tinkerpop-provider-development/references/architecture.md),
[change guidelines](.agents/skills/tinkerpop-provider-development/references/changes.md),
and [CI-aligned testing](.agents/skills/tinkerpop-provider-development/references/testing.md).
These files are also usable directly by human contributors. `CLAUDE.md` and
`GEMINI.md` import the same root instructions; keep those entrypoints free of
independent rules.

## Start DB
```
./docker/start_db.sh
```

## SonarCloud
Check results [here](https://sonarcloud.io/project/overview?id=arangodb_arangodb-tinkerpop-provider).

## check dependencies updates
```shell
mvn versions:display-dependency-updates -Pstatic-code-analysis -Prelease
mvn versions:display-plugin-updates -Pstatic-code-analysis -Prelease
```
