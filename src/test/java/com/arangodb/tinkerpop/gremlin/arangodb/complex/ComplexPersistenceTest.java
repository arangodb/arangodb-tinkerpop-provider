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

package com.arangodb.tinkerpop.gremlin.arangodb.complex;

import com.arangodb.ArangoCollection;
import com.arangodb.tinkerpop.gremlin.TestGraphClient;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.tinkerpop.gremlin.utils.Fields;
import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ComplexPersistenceTest extends AbstractGremlinTest {

    private TestGraphClient client() {
        return new TestGraphClient(graph.configuration());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void vertices() {
        Vertex v = graph.addVertex(T.id, Vertex.DEFAULT_LABEL + "/foo");
        v
                .property("key", "value")
                .property("meta", "metaValue");
        ArangoCollection col = client().database().collection(Vertex.DEFAULT_LABEL);
        Map<String, Object> doc = (Map<String, Object>) col.getDocument(((ArangoDBGraph) graph).elementId(v).getKey(), Map.class);
        assertThat(doc)
                .hasSize(5)
                .containsEntry(Fields.KEY, "foo")
                .containsEntry(Fields.ID, Vertex.DEFAULT_LABEL + "/foo")
                .containsKey(Fields.REV)
                .doesNotContainKey(Fields.LABEL)
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
        Vertex a = graph.addVertex(T.id, Vertex.DEFAULT_LABEL + "/a");
        Vertex b = graph.addVertex(T.id, Vertex.DEFAULT_LABEL + "/b");
        Edge e = a.addEdge(Edge.DEFAULT_LABEL, b, T.id, Edge.DEFAULT_LABEL + "/e", "key", "value");

        ArangoCollection col = client().database().collection(Edge.DEFAULT_LABEL);
        Map<String, Object> doc = (Map<String, Object>) col.getDocument(((ArangoDBGraph) graph).elementId(e).getKey(), Map.class);
        assertThat(doc)
                .hasSize(6)
                .containsEntry(Fields.KEY, "e")
                .containsEntry(Fields.ID, Edge.DEFAULT_LABEL + "/e")
                .containsKey(Fields.REV)
                .containsEntry(Fields.FROM, Vertex.DEFAULT_LABEL + "/a")
                .containsEntry(Fields.TO, Vertex.DEFAULT_LABEL + "/b")
                .doesNotContainKey(Fields.LABEL)
                .containsEntry("key", "value");
    }
}
