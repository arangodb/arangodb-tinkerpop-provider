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

import com.arangodb.entity.GraphEntity;
import com.arangodb.tinkerpop.gremlin.DataDefinitionTest;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig.EdgeDef;
import org.apache.commons.configuration2.Configuration;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ComplexDataDefinitionTest extends DataDefinitionTest {

    @Override
    protected ArangoDBGraphConfig.GraphType graphType() {
        return ArangoDBGraphConfig.GraphType.COMPLEX;
    }

    @Test
    public void complexEmptyGraph() {
        Configuration conf = confBuilder().build();
        GraphEntity graphInfo = graphInfo(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions()).isEmpty();
    }

    @Test
    public void complexGraphWithoutEdges() {
        Configuration conf = confBuilder()
                .name("foo")
                .orphanCollections("v")
                .build();
        GraphEntity graphInfo = graphInfo(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections())
                .hasSize(1)
                .contains("v");
        assertThat(graphInfo.getEdgeDefinitions()).isEmpty();
    }

    @Test
    public void complexGraph() {
        String name = "complexGraph";
        Configuration conf = confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build();
        GraphEntity graphInfo = graphInfo(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions())
                .hasSize(1)
                .allSatisfy(ed -> {
                    assertThat(ed.getCollection()).isEqualTo("e");
                    assertThat(ed.getFrom())
                            .hasSize(1)
                            .contains("v");
                    assertThat(ed.getTo())
                            .hasSize(1)
                            .contains("v");
                });
    }

    @Test
    public void complexGraphWithOrphanCollections() {
        String name = "complexGraph";
        Configuration conf = confBuilder()
                .name(name)
                .orphanCollections("a")
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build();
        GraphEntity graphInfo = graphInfo(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections())
                .hasSize(1)
                .contains("a");
        assertThat(graphInfo.getEdgeDefinitions())
                .hasSize(1)
                .allSatisfy(ed -> {
                    assertThat(ed.getCollection()).isEqualTo("e");
                    assertThat(ed.getFrom())
                            .hasSize(1)
                            .contains("v");
                    assertThat(ed.getTo())
                            .hasSize(1)
                            .contains("v");
                });
    }

    @Test
    public void complexGraphWithManyEdgesCollections() {
        String name = "complexGraph";
        Configuration conf = confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("e").from("v", "a").to("a", "v"))
                .build();
        GraphEntity graphInfo = graphInfo(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions())
                .hasSize(1)
                .allSatisfy(ed -> {
                    assertThat(ed.getCollection()).isEqualTo("e");
                    assertThat(ed.getFrom())
                            .hasSize(2)
                            .contains("a")
                            .contains("v");
                    assertThat(ed.getTo())
                            .hasSize(2)
                            .contains("a")
                            .contains("v");
                });
    }

    @Test
    public void complexGraphWithManyEdgesCollectionsWithSameName() {
        String name = "complexGraph";
        Configuration conf = confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .edgeDefinitions(EdgeDef.of("e").from("a").to("a"))
                .build();
        GraphEntity graphInfo = graphInfo(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions())
                .hasSize(1)
                .allSatisfy(ed -> {
                    assertThat(ed.getCollection()).isEqualTo("e");
                    assertThat(ed.getFrom())
                            .hasSize(2)
                            .contains("a")
                            .contains("v");
                    assertThat(ed.getTo())
                            .hasSize(2)
                            .contains("a")
                            .contains("v");
                });
    }

    @Test
    public void existingComplexGraphWithManyEdgesCollectionsWithSameNameInDifferentOrder() {
        String name = "complexGraph";
        graphInfo(confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("e").from("a").to("a"))
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build());
        Configuration conf = confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .edgeDefinitions(EdgeDef.of("e").from("a").to("a"))
                .build();
        GraphEntity graphInfo = graphInfo(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions())
                .hasSize(1)
                .allSatisfy(ed -> {
                    assertThat(ed.getCollection()).isEqualTo("e");
                    assertThat(ed.getFrom())
                            .hasSize(2)
                            .contains("a")
                            .contains("v");
                    assertThat(ed.getTo())
                            .hasSize(2)
                            .contains("a")
                            .contains("v");
                });
    }

    @Test
    public void existingComplexGraph() {
        String name = "existingComplexGraph";
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
                    assertThat(ed.getCollection()).isEqualTo("e");
                    assertThat(ed.getFrom())
                            .hasSize(1)
                            .contains("v");
                    assertThat(ed.getTo())
                            .hasSize(1)
                            .contains("v");
                });
    }

    @Test
    public void existingComplexGraphWithMoreOrphanCollections() {
        String name = "existingComplexGraph";
        graphInfo(confBuilder()
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
    public void existingComplexGraphWithLessOrphanCollections() {
        String name = "existingComplexGraph";
        graphInfo(confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build());
        Configuration conf = confBuilder()
                .name(name)
                .orphanCollections("a")
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
    public void existingComplexGraphWithMoreEdgeDefinitions() {
        String name = "existingComplexGraph";
        graphInfo(confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("b"))
                .edgeDefinitions(EdgeDef.of("y").from("b").to("a"))
                .build());
        Configuration conf = confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("b"))
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
    public void existingComplexGraphWithLessEdgeDefinitions() {
        String name = "existingComplexGraph";
        graphInfo(confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("b"))
                .build());
        Configuration conf = confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("b"))
                .edgeDefinitions(EdgeDef.of("y").from("b").to("a"))
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
    public void existingComplexGraphWithMismatchingEdgeDefinitions() {
        String name = "existingComplexGraph";
        graphInfo(confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("b"))
                .build());
        Configuration conf = confBuilder()
                .name(name)
                .edgeDefinitions(EdgeDef.of("x").from("b").to("a"))
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

}
