#!/bin/bash

LOCATION=$(pwd)/$(dirname "$0")
TARGET=/opt/gremlin-console
JAR=$(ls $LOCATION/../target/arangodb-tinkerpop-provider-*-standalone.jar 2>/dev/null | head -n 1)

echo "LOCATION: $LOCATION"
echo "TARGET: $TARGET"
echo "JAR: $JAR"

docker run \
  -v $LOCATION/test.groovy:$TARGET/test.groovy \
  -v $LOCATION/arangodb.yaml:$TARGET/conf/arangodb.yaml \
  -v $JAR:$TARGET/ext/arangodb-tinkerpop-provider/plugin/arangodb-tinkerpop-provider.jar \
  docker.io/tinkerpop/gremlin-console \
  -e $TARGET/test.groovy
