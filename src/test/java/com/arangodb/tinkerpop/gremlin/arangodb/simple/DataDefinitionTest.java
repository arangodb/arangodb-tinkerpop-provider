package com.arangodb.tinkerpop.gremlin.arangodb.simple;

import com.arangodb.tinkerpop.gremlin.AbstractTest;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class DataDefinitionTest extends AbstractTest {

    @Test
    public void shouldCreateGraph() {
        ArangoDBGraph g = createGraph(confBuilder()
                .enableDataDefinition(true)
                .graph("foo")
                .build());
        assertThat(g.name()).isEqualTo("foo");
    }

    @Test
    public void shouldNotCreateGraphIfDataDefinitionDisabled() {
        Throwable thrown = catchThrowable(() -> createGraph(confBuilder()
                .enableDataDefinition(false)
                .graph("foo")
                .build()));
        assertThat(thrown.getCause().getCause())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Graph [foo] not found")
                .hasMessageContaining("To enable creation set: graph.enableDataDefinition=true");
    }

}
