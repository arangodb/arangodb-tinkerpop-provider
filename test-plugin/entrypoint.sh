#!/bin/bash

set -e

/opt/gremlin-server/bin/gremlin-server.sh install com.arangodb arangodb-tinkerpop-provider 0.0.1-SNAPSHOT
/opt/gremlin-server/bin/gremlin-server.sh /arangodb/gremlin-server-arangodb.yaml
