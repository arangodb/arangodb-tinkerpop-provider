#!/bin/bash

docker rm -f tinkerpop-data gremlin-server gremlin-console
set -e

LOCATION=$(pwd)/$(dirname "$0")
echo "LOCATION: $LOCATION"

docker create \
  -v /tmp/docker \
  -v /arangodb \
  -v /home/gremlin/.m2/repository/com/arangodb \
  --name tinkerpop-data alpine:3 /bin/true

docker cp "$LOCATION"/entrypoint.sh tinkerpop-data:/tmp/docker
docker cp "$LOCATION"/test.groovy tinkerpop-data:/arangodb
docker cp "$LOCATION"/gremlin-server-arangodb.yaml tinkerpop-data:/arangodb
docker cp "$LOCATION"/arangodb.yaml tinkerpop-data:/arangodb
docker cp "$LOCATION"/remote.yaml tinkerpop-data:/arangodb
docker cp ~/.m2/repository/com/arangodb/arangodb-tinkerpop-provider tinkerpop-data:/home/gremlin/.m2/repository/com/arangodb

docker run -d \
  --network arangodb \
  --volumes-from tinkerpop-data \
  --entrypoint /tmp/docker/entrypoint.sh \
  --name gremlin-server \
  --expose 8182 \
  -p 172.28.0.1:8182:8182 \
  docker.io/tinkerpop/gremlin-server

while ! nc -z 172.28.0.1 8182; do
  printf '.'
  sleep 1
done

echo "Gremlin Server ready!"

docker run \
  --network arangodb \
  --volumes-from tinkerpop-data \
  --name gremlin-console \
  docker.io/tinkerpop/gremlin-console \
  -e /arangodb/test.groovy
