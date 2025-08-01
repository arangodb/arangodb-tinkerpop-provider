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

import com.arangodb.Protocol;
import com.arangodb.tinkerpop.gremlin.structure.*;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.AbstractGraphProvider;
import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class TestGraphProvider extends AbstractGraphProvider {

    private final String dbName = getClass().getSimpleName();

    protected abstract void customizeBuilder(ArangoDBConfigurationBuilder builder);

    protected void configureDataDefinitions(ArangoDBConfigurationBuilder builder,
                                            Class<?> test,
                                            String testMethodName,
                                            LoadGraphWith.GraphData loadGraphWith) {
    }

    public ArangoDBConfigurationBuilder confBuilder() {
        ArangoDBConfigurationBuilder builder = new ArangoDBConfigurationBuilder()
                .enableDataDefinition(true)
                .hosts("172.28.0.1:8529")
                .user("root")
                .password("test")
                .protocol(Protocol.HTTP2_VPACK)
                .database(dbName);
        customizeBuilder(builder);
        return builder;
    }

    @Override
    public Configuration newGraphConfiguration(final String graphName, final Class<?> test,
                                               final String testMethodName,
                                               final Map<String, Object> configurationOverrides,
                                               final LoadGraphWith.GraphData loadGraphWith) {
        ArangoDBConfigurationBuilder builder = confBuilder().graph(graphName);
        configureDataDefinitions(builder, test, testMethodName, loadGraphWith);
        return builder.build();
    }

    @Override
    public void clear(Graph graph, Configuration configuration) throws Exception {
        TestGraphClient client = new TestGraphClient(configuration);
        client.clear(new ArangoDBGraphConfig(configuration).graphName);
        client.shutdown();
        if (graph != null) {
            graph.close();
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Set<Class> getImplementations() {
        return Stream.of(
                ArangoDBEdge.class,
                ArangoDBElement.class,
                ArangoDBGraph.class,
                ArangoDBGraphVariables.class,
                ArangoDBPersistentElement.class,
                ArangoDBProperty.class,
                ArangoDBSimpleElement.class,
                ArangoDBVertex.class,
                ArangoDBVertexProperty.class
        ).collect(Collectors.toSet());
    }

    @Override
    public Map<String, Object> getBaseConfiguration(String graphName, Class<?> test, String testMethodName,
                                                    LoadGraphWith.GraphData loadGraphWith) {
        return null;
    }

    @Override
    public Object convertId(Object id, Class<? extends Element> c) {
        return id.toString();
    }

}
