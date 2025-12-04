#!/bin/bash

docker rm -f tinkerpop-data
set -e

LOCATION=$(pwd)/$(dirname "$0")
echo "LOCATION: $LOCATION"

JAR=$(ls $LOCATION/../target/arangodb-tinkerpop-provider-*-standalone.jar 2>/dev/null | head -n 1)
echo "JAR: $JAR"

docker create \
  -v /arangodb \
  -v /opt/gremlin-console/ext/arangodb-tinkerpop-provider/plugin \
  --name tinkerpop-data alpine:3 /bin/true

docker cp "$LOCATION"/test.groovy tinkerpop-data:/arangodb
docker cp $JAR tinkerpop-data:/opt/gremlin-console/ext/arangodb-tinkerpop-provider/plugin
docker cp "$LOCATION"/arangodb.yaml tinkerpop-data:/arangodb

docker run \
  --volumes-from tinkerpop-data \
  docker.io/tinkerpop/gremlin-console:3.7.5 \
  -e /arangodb/test.groovy
