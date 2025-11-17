/*
 * Copyright 2025 ArangoDB GmbH and The University of York
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.tinkerpop.gremlin;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.ArangoGraph;
import com.arangodb.tinkerpop.gremlin.client.ArangoDBGraphClient;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import org.apache.commons.configuration2.Configuration;


public class TestGraphClient extends ArangoDBGraphClient {

    public TestGraphClient(Configuration config) {
        super(new ArangoDBGraphConfig(config), null, null);
        if (!db.exists()) {
            db.create();
        }
        ArangoCollection varsCol = db.collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION);
        if (!varsCol.exists()) {
            varsCol.create();
        }
    }

    public void clear(String name) {
        try {
            db.collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION).deleteDocument(name);
        } catch (ArangoDBException e) {
            Integer errNum = e.getErrorNum();
            if (errNum == null || (
                    errNum != 1202              // document not found
                            && errNum != 1203   // collection not found
            )) {
                throw e;
            }
        }

        ArangoGraph g = db.graph(name);
        if (g.exists()) {
            g.drop(true);
        }
    }

    public ArangoDatabase database() {
        return db;
    }

    public ArangoCollection variablesCollection() {
        return db.collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION);
    }

}
