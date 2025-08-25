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

package com.arangodb.tinkerpop.gremlin.simple;

import com.arangodb.tinkerpop.gremlin.TestGraphProvider;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;
import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.algorithm.generator.CommunityGeneratorTest;
import org.apache.tinkerpop.gremlin.algorithm.generator.DistributionGeneratorTest;
import org.apache.tinkerpop.gremlin.structure.io.IoGraphTest;
import org.apache.tinkerpop.gremlin.structure.io.IoTest;

public class SimpleGraphProvider extends TestGraphProvider {

    @Override
    protected void customizeBuilder(ArangoDBConfigurationBuilder builder) {
        builder
                .graphType(ArangoDBGraphConfig.GraphType.SIMPLE)
                .graphClass(SimpleTestGraph.class);
    }

    @Override
    protected void configureDataDefinitions(ArangoDBConfigurationBuilder builder, Class<?> test, String testMethodName, LoadGraphWith.GraphData loadGraphWith) {
        if (CommunityGeneratorTest.class.equals(test.getEnclosingClass()) ||
                DistributionGeneratorTest.class.equals(test.getEnclosingClass()) ||
                IoTest.class.equals(test.getEnclosingClass()) ||
                IoGraphTest.class.equals(test)) {
            String name = builder.getConfig().getString("gremlin.arangodb.conf.graph.name");
            switch (name) {
                case "readGraph":
                    builder.db("SimpleGraphProvider-readGraph");
                    break;
                case "target":
                    builder.db("SimpleGraphProvider-target");
                    break;
                case "g1":
                    builder.db("SimpleGraphProvider-g1");
                    break;
                case "g2":
                    builder.db("SimpleGraphProvider-g2");
                    break;
            }
        }
    }
}
