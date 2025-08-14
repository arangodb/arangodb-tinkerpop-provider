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

package com.arangodb.tinkerpop.gremlin.arangodb.simple;

import com.arangodb.ArangoCollection;
import com.arangodb.tinkerpop.gremlin.PackageVersion;
import com.arangodb.tinkerpop.gremlin.TestGraphClient;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.tinkerpop.gremlin.utils.Fields;
import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.Map;

import static com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph.GRAPH_VARIABLES_COLLECTION;
import static org.assertj.core.api.Assertions.assertThat;

public class SimplePersistenceTest extends AbstractGremlinTest {

    private TestGraphClient client() {
        return new TestGraphClient(graph.configuration());
    }

    private String graphName() {
        return ((ArangoDBGraph) graph).name();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void variables() {
        graph.variables().set("key", "value");
        ArangoCollection col = client().variablesCollection();
        Map<String, Object> doc = (Map<String, Object>) col.getDocument(graphName(), Map.class);
        assertThat(doc)
                .hasSize(5)
                .containsEntry(Fields.ID, GRAPH_VARIABLES_COLLECTION + "/" + graphName())
                .containsEntry(Fields.KEY, graphName())
                .containsKey(Fields.REV)
                .containsEntry(Fields.VERSION, PackageVersion.VERSION)
                .containsEntry("key", "value");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void vertices() {
        Vertex v = graph.addVertex(
                T.id, "foo",
                T.label, "bar"
        );
        v
                .property("key", "value")
                .property("meta", "metaValue");
        String colName = graphName() + "_" + Vertex.DEFAULT_LABEL;
        ArangoCollection col = client().database().collection(colName);
        Map<String, Object> doc = (Map<String, Object>) col.getDocument((String) v.id(), Map.class);
        assertThat(doc)
                .hasSize(6)
                .containsEntry(Fields.KEY, "foo")
                .containsEntry(Fields.ID, colName + "/foo")
                .containsKey(Fields.REV)
                .containsEntry(Fields.LABEL, "bar")
                .containsEntry("key", "value");

        Map<String, Map<String, Object>> meta = (Map<String, Map<String, Object>>) doc.get(Fields.META);
        assertThat(meta)
                .hasSize(1)
                .containsKey("key");

        Map<String, Object> fooMeta = meta.get("key");
        assertThat(fooMeta)
                .containsEntry("meta", "metaValue");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void edges() {
        Vertex a = graph.addVertex(T.id, "a");
        Vertex b = graph.addVertex(T.id, "b");
        Edge e = a.addEdge("foo", b, T.id, "e", "key", "value");

        String vertexColName = graphName() + "_" + Vertex.DEFAULT_LABEL;
        String edgeColName = graphName() + "_" + Edge.DEFAULT_LABEL;
        ArangoCollection col = client().database().collection(edgeColName);
        Map<String, Object> doc = (Map<String, Object>) col.getDocument((String) e.id(), Map.class);
        assertThat(doc)
                .hasSize(7)
                .containsEntry(Fields.KEY, "e")
                .containsEntry(Fields.ID, edgeColName + "/e")
                .containsKey(Fields.REV)
                .containsEntry(Fields.FROM, vertexColName + "/a")
                .containsEntry(Fields.TO, vertexColName + "/b")
                .containsEntry(Fields.LABEL, "foo")
                .containsEntry("key", "value");
    }
}
