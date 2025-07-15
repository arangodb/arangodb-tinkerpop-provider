/*
 * Copyright 2025 ArangoDB GmbH, Cologne, Germany
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

import com.arangodb.Protocol;
import com.arangodb.entity.GraphEntity;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;
import org.junit.After;
import org.junit.Before;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractTest {
    protected TestGraphClient client;
    protected final String dbName = getClass().getSimpleName();
    private final Set<ArangoDBGraph> graphs = ConcurrentHashMap.newKeySet();

    @Before
    public void initDB() {
        client = new TestGraphClient(confBuilder().build());
    }

    @After
    public void clear() {
        for (ArangoDBGraph graph : graphs) {
            graph.close();
            client.clear(graph.name());
        }
        client.shutdown();
    }

    protected ArangoDBConfigurationBuilder confBuilder() {
        return new ArangoDBConfigurationBuilder()
                .enableDataDefinition(true)
                .hosts("172.28.0.1:8529")
                .user("root")
                .password("test")
                .protocol(Protocol.HTTP2_VPACK)
                .database(dbName);
    }

    @SuppressWarnings("resource")
    protected GraphEntity graphInfo(Configuration conf) {
        return createGraph(conf).getClient().getArangoGraph().getInfo();
    }

    protected ArangoDBGraph createGraph(Configuration conf) {
        ArangoDBGraph g = (ArangoDBGraph) GraphFactory.open(conf);
        graphs.add(g);
        return g;
    }

    protected ArangoDBGraph createGraph(String configurationFile) {
        ArangoDBGraph g = (ArangoDBGraph) GraphFactory.open(configurationFile);
        graphs.add(g);
        return g;
    }

    protected String getName(Configuration conf) {
        return new ArangoDBGraphConfig(conf).graphName;
    }

}
