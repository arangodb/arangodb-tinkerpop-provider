package com.arangodb.tinkerpop.gremlin.arangodb.simple;

import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("resource")
public class DriverTest extends AbstractGremlinTest {

    private ArangoDBGraph graph() {
        return (ArangoDBGraph) this.graph;
    }

    @Test
    public void shouldGetDriverVersion() {
        assertThat(graph().getArangoDriver().getVersion()).isNotNull();
    }

    @Test
    public void shouldGetDatabaseInfo() {
        assertThat(graph().getArangoDatabase().getInfo().getName())
                .isEqualTo("SimpleGraphProvider");
    }

    @Test
    public void shouldGetGraphInfo() {
        assertThat(graph().getArangoGraph().getInfo().getName())
                .isEqualTo("standard");
    }
}