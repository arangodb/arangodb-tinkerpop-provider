#!/bin/bash

set -e

/opt/gremlin-server/bin/gremlin-server.sh install com.arangodb arangodb-tinkerpop-provider 4.0.0
/opt/gremlin-server/bin/gremlin-server.sh /arangodb/gremlin-server-arangodb.yaml
