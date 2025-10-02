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

package com.arangodb.tinkerpop.gremlin.complex;

import com.arangodb.tinkerpop.gremlin.process.traversal.strategy.optimization.ArangoStepStrategy;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.structure.Graph;

@Graph.OptIn("com.arangodb.tinkerpop.gremlin.arangodb.complex.ComplexArangoDBSuite")
public class ComplexTestGraph extends ArangoDBGraph {

    static {
        TraversalStrategies.GlobalCache.registerStrategies(ComplexTestGraph.class, TraversalStrategies.GlobalCache.getStrategies(Graph.class).clone()
                .addStrategies(ArangoStepStrategy.INSTANCE));
    }

    @SuppressWarnings("unused")
    public static ComplexTestGraph open(Configuration configuration) {
        return new ComplexTestGraph(configuration);
    }

    public ComplexTestGraph(Configuration configuration) {
        super(configuration);
    }
}
