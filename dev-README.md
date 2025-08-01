# dev-README

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
