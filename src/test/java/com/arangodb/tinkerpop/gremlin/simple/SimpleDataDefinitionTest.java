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

import com.arangodb.entity.GraphEntity;
import com.arangodb.tinkerpop.gremlin.DataDefinitionTest;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig.EdgeDef;
import org.apache.commons.configuration2.Configuration;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class SimpleDataDefinitionTest extends DataDefinitionTest {

    @Override
    protected ArangoDBGraphConfig.GraphType graphType() {
        return ArangoDBGraphConfig.GraphType.SIMPLE;
    }

    @Test
    public void simpleEmptyGraph() {
        Configuration conf = confBuilder().build();
        checkDefaultSimpleGraph(conf);
    }

    @Test
    public void simpleGraph() {
        Configuration conf = confBuilder()
                .edgeDefinitions(EdgeDef.of("edge").from("vertex").to("vertex"))
                .build();
        checkDefaultSimpleGraph(conf);
    }

    @Test
    public void simpleGraphWithNonDefaultCollections() {
        Configuration conf = confBuilder()
                .name("foo")
                .edgeDefinitions(EdgeDef.of("edges").from("vertexes").to("vertexes"))
                .build();
        GraphEntity graphInfo = graphInfo(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions())
                .hasSize(1)
                .allSatisfy(ed -> {
                    assertThat(ed.getCollection()).isEqualTo("foo_edges");
                    assertThat(ed.getFrom())
                            .hasSize(1)
                            .contains("foo_vertexes");
                    assertThat(ed.getTo())
                            .hasSize(1)
                            .contains("foo_vertexes");
                });
    }

    @Test
    public void simpleGraphWithInvalidVertexName() {
        Configuration conf = confBuilder()
                .name("foo")
                .edgeDefinitions(EdgeDef.of("edge").from("foo_ver_tex").to("foo_vertex"))
                .build();
        Throwable thrown = catchThrowable(() -> graphInfo(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name cannot contain '_'");
    }

    @Test
    public void simpleGraphWithInvalidEdgeName() {
        Configuration conf = confBuilder()
                .name("foo")
                .edgeDefinitions(EdgeDef.of("foo_ed_ge").from("vertex").to("vertex"))
                .build();
        Throwable thrown = catchThrowable(() -> graphInfo(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name cannot contain '_'");
    }

    @Test
    public void existingSimpleGraph() {
        String name = "existingSimpleGraph";
        Configuration conf = confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build();
        graphInfo(conf);
        GraphEntity graphInfo = graphInfo(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions())
                .hasSize(1)
                .allSatisfy(ed -> {
                    assertThat(ed.getCollection()).isEqualTo(name + "_e");
                    assertThat(ed.getFrom())
                            .hasSize(1)
                            .contains(name + "_v");
                    assertThat(ed.getTo())
                            .hasSize(1)
                            .contains(name + "_v");
                });
    }

    @Test
    public void existingGraphWithMoreOrphanCollections() {
        String name = "existingSimpleGraph";
        graphInfo(confBuilder()
                .graphType(ArangoDBGraphConfig.GraphType.COMPLEX)
                .name(name)
                .orphanCollections("a")
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build());
        Configuration conf = confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build();
        Throwable thrown = catchThrowable(() -> graphInfo(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Orphan collections do not match");
    }

    @Test
    public void existingGraphWithLessOrphanCollections() {
        String name = "existingSimpleGraph";
        graphInfo(confBuilder()
                .graphType(ArangoDBGraphConfig.GraphType.COMPLEX)
                .name(name)
                .build());
        Configuration conf = confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build();
        Throwable thrown = catchThrowable(() -> graphInfo(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Edge definitions do not match");
    }

    @Test
    public void existingSimpleGraphWithMoreEdgeDefinitions() {
        String name = "existingSimpleGraph";
        graphInfo(confBuilder()
                .graphType(ArangoDBGraphConfig.GraphType.COMPLEX)
                .name(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("a"))
                .edgeDefinitions(EdgeDef.of("y").from("a").to("a"))
                .build());
        Configuration conf = confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("a"))
                .build();
        Throwable thrown = catchThrowable(() -> graphInfo(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Edge definitions do not match");
    }

    @Test
    public void existingSimpleGraphWithLessEdgeDefinitions() {
        String name = "existingSimpleGraph";
        graphInfo(confBuilder()
                .graphType(ArangoDBGraphConfig.GraphType.COMPLEX)
                .name(name)
                .orphanCollections("a")
                .build());
        Configuration conf = confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("a"))
                .build();
        Throwable thrown = catchThrowable(() -> graphInfo(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Orphan collections do not match");
    }

    private void checkDefaultSimpleGraph(Configuration conf) {
        String name = getName(conf);
        GraphEntity graphInfo = graphInfo(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions())
                .hasSize(1)
                .allSatisfy(ed -> {
                    assertThat(ed.getCollection()).isEqualTo(name + "_edge");
                    assertThat(ed.getFrom())
                            .hasSize(1)
                            .contains(name + "_vertex");
                    assertThat(ed.getTo())
                            .hasSize(1)
                            .contains(name + "_vertex");
                });
    }

}
