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

import com.arangodb.tinkerpop.gremlin.structure.ArangoDBEdge;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBVertex;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;

@Graph.OptIn(Graph.OptIn.SUITE_STRUCTURE_STANDARD)
@Graph.OptIn(Graph.OptIn.SUITE_PROCESS_STANDARD)
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.GraphTest",
        method = "shouldAddVertexWithUserSuppliedStringId",
        reason = "tested with simple graph only")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.GraphTest",
        method = "shouldRemoveVertices",
        reason = "tested with simple graph only")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.GraphTest",
        method = "shouldRemoveEdges",
        reason = "tested with simple graph only")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.GraphTest",
        method = "shouldEvaluateConnectivityPatterns",
        reason = "tested with simple graph only")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.VertexTest$AddEdgeTest",
        method = "shouldAddEdgeWithUserSuppliedStringId",
        reason = "tested with simple graph only")
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
public class ComplexTestGraphWithoutIdPrefix extends ArangoDBGraph {

    @SuppressWarnings("unused")
    public static ComplexTestGraphWithoutIdPrefix open(Configuration configuration) {
        return new ComplexTestGraphWithoutIdPrefix(configuration);
    }

    public ComplexTestGraphWithoutIdPrefix(Configuration configuration) {
        super(configuration);
    }

    @Override
    public ArangoDBVertex createVertex(Object... keyValues) {
        ElementHelper.legalPropertyKeyValueArray(keyValues);
        String label = ElementHelper.getLabelValue(keyValues).orElse(Vertex.DEFAULT_LABEL);
        return super.createVertex(transformKeyValues(label, keyValues));
    }

    @Override
    public ArangoDBEdge createEdge(String label, Vertex outVertex, Vertex inVertex, Object... keyValues) {
        ElementHelper.legalPropertyKeyValueArray(keyValues);
        return super.createEdge(label, outVertex, inVertex, transformKeyValues(label, keyValues));
    }

    private Object[] transformKeyValues(String label, Object[] keyValues) {
        Object[] newKeyValues = new Object[keyValues.length];
        for (int i = 0; i < keyValues.length; i = i + 2) {
            Object key = keyValues[i];
            Object value = keyValues[i + 1];
            if (key.equals(T.id) && value instanceof String) {
                String prefix = label + "/";
                String id = (String) value;
                if (!id.startsWith(prefix)) {
                    value = prefix + id;
                }
            }
            newKeyValues[i] = key;
            newKeyValues[i + 1] = value;
        }
        return newKeyValues;
    }
}
