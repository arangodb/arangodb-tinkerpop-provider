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

import com.arangodb.tinkerpop.gremlin.process.traversal.strategy.optimization.ArangoStepStrategy;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.structure.Graph;

@Graph.OptIn(Graph.OptIn.SUITE_STRUCTURE_STANDARD)
@Graph.OptIn(Graph.OptIn.SUITE_PROCESS_STANDARD)
@Graph.OptIn("com.arangodb.tinkerpop.gremlin.custom.CustomStandardSuite")
@Graph.OptIn("com.arangodb.tinkerpop.gremlin.arangodb.simple.SimpleArangoDBSuite")
@Graph.OptIn("com.arangodb.tinkerpop.gremlin.ssl.SslSuite")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.util.detached.DetachedGraphTest",
        method = "testAttachableCreateMethod",
        reason = "replaced by com.arangodb.tinkerpop.gremlin.simple.custom.structure.util.detached.DetachedGraphTest#testAttachableCreateMethod()")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.util.star.StarGraphTest",
        method = "shouldAttachWithCreateMethod",
        reason = "replaced by com.arangodb.tinkerpop.gremlin.simple.custom.structure.util.star.StarGraphTest.shouldAttachWithCreateMethod")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.map.MergeEdgeTest$Traversals",
        method = "*",
        reason = "replaced by com.arangodb.tinkerpop.gremlin.simple.custom.process.traversal.step.map.MergeEdgeTest")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.OrderabilityTest$Traversals",
        method = "g_V_properties_order",
        reason = "requires numeric ids support")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.OrderabilityTest$Traversals",
        method = "g_V_out_outE_order_byXascX",
        reason = "requires numeric ids support")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.OrderabilityTest$Traversals",
        method = "g_V_out_outE_order_byXdescX",
        reason = "requires numeric ids support")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.OrderabilityTest$Traversals",
        method = "g_V_out_outE_asXheadX_path_order_byXascX_selectXheadX",
        reason = "requires numeric ids support")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.OrderabilityTest$Traversals",
        method = "g_V_out_out_properties_asXheadX_path_order_byXdescX_selectXheadX_value",
        reason = "requires numeric ids support")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.OrderabilityTest$Traversals",
        method = "g_V_out_outE_asXheadX_path_order_byXdescX_selectXheadX",
        reason = "requires numeric ids support")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.OrderabilityTest$Traversals",
        method = "g_V_out_out_properties_asXheadX_path_order_byXascX_selectXheadX_value",
        reason = "requires numeric ids support")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.SubgraphTest$Traversals",
        method = "g_V_withSideEffectXsgX_repeatXbothEXcreatedX_subgraphXsgX_outVX_timesX5X_name_dedup",
        reason = "requires VertexProperty user supplied identifiers")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.SubgraphTest$Traversals",
        method = "g_V_withSideEffectXsgX_outEXknowsX_subgraphXsgX_name_capXsgX",
        reason = "requires VertexProperty user supplied identifiers")
public class SimpleTestGraph extends ArangoDBGraph {

    static {
        TraversalStrategies.GlobalCache.registerStrategies(SimpleTestGraph.class, TraversalStrategies.GlobalCache.getStrategies(Graph.class).clone()
                .addStrategies(ArangoStepStrategy.INSTANCE));
    }

    @SuppressWarnings("unused")
    public static SimpleTestGraph open(Configuration configuration) {
        return new SimpleTestGraph(configuration);
    }

    public SimpleTestGraph(Configuration configuration) {
        super(configuration);
    }
}
