#!/bin/bash

set -e

/opt/gremlin-server/bin/gremlin-server.sh install com.arangodb arangodb-tinkerpop-provider 3.1.0-SNAPSHOT
/opt/gremlin-server/bin/gremlin-server.sh /arangodb/gremlin-server-arangodb.yaml
