#!/bin/bash

set -e

/opt/gremlin-server/bin/gremlin-server.sh install com.arangodb arangodb-tinkerpop-provider 3.3.0
/opt/gremlin-server/bin/gremlin-server.sh /arangodb/gremlin-server-arangodb.yaml
