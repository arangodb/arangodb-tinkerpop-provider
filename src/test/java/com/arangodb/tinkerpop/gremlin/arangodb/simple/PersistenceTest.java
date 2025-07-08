package com.arangodb.tinkerpop.gremlin.arangodb.simple;

import com.arangodb.ArangoCollection;
import com.arangodb.tinkerpop.gremlin.PackageVersion;
import com.arangodb.tinkerpop.gremlin.TestGraphClient;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.Map;

import static com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph.GRAPH_VARIABLES_COLLECTION;
import static org.assertj.core.api.Assertions.assertThat;

public class PersistenceTest extends AbstractGremlinTest {

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
                .containsEntry("_id", GRAPH_VARIABLES_COLLECTION + "/" + graphName())
                .containsEntry("_key", graphName())
                .containsKey("_rev")
                .containsEntry("_version", PackageVersion.VERSION)
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
                .containsEntry("_key", "foo")
                .containsEntry("_id", colName + "/foo")
                .containsKey("_rev")
                .containsEntry("_label", "bar")
                .containsEntry("key", "value");

        Map<String, Map<String, Object>> meta = (Map<String, Map<String, Object>>) doc.get("_meta");
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
                .containsEntry("_key", "e")
                .containsEntry("_id", edgeColName + "/e")
                .containsKey("_rev")
                .containsEntry("_from", vertexColName + "/a")
                .containsEntry("_to", vertexColName + "/b")
                .containsEntry("_label", "foo")
                .containsEntry("key", "value");
    }
}
