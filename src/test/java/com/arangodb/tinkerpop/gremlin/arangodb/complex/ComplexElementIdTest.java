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

import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ComplexElementIdTest extends AbstractGremlinTest {

    @Test
    public void id() {
        assertThat(graph.addVertex(T.id, "foo/a").id()).isEqualTo("foo/a");
        assertThat(graph.addVertex(T.id, "foo/b", T.label, "foo").id()).isEqualTo("foo/b");
        assertThat(graph.addVertex(T.label, "foo").id())
                .isInstanceOf(String.class)
                .asString()
                .startsWith("foo/");
        assertThat(graph.addVertex().id())
                .isInstanceOf(String.class)
                .asString()
                .startsWith(Vertex.DEFAULT_LABEL + "/");

        assertThat(catchThrowable(() -> graph.addVertex(T.id, "/c")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must start with label prefix")
                .hasMessageContaining("<label>/");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "c/")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must have format")
                .hasMessageContaining("<label>/<key>");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "c", T.label, "foo")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must start with label prefix")
                .hasMessageContaining("foo/");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "d")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must start with label prefix")
                .hasMessageContaining("<label>/");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "foo/bar", T.label, "baz")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must start with label prefix")
                .hasMessageContaining("baz/");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "foo/bar/baz")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bar/baz")
                .hasMessageContaining("invalid character '/'");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "foo_bar")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foo_bar")
                .hasMessageContaining("invalid character '_'");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "foo/bar_baz")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bar_baz")
                .hasMessageContaining("invalid character '_'");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "foo_bar/baz")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foo_bar")
                .hasMessageContaining("invalid character '_'");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "vertex_foo/bar")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vertex_foo")
                .hasMessageContaining("invalid character '_'");
    }

    @Test
    public void label() {
        assertThat(graph.addVertex(T.id, "foo/a").label()).isEqualTo("foo");
        assertThat(graph.addVertex(T.id, "foo/b", T.label, "foo").label()).isEqualTo("foo");
        assertThat(graph.addVertex(T.label, "foo").label()).isEqualTo("foo");
        assertThat(graph.addVertex().label()).isEqualTo(Vertex.DEFAULT_LABEL);

        assertThat(catchThrowable(() -> graph.addVertex(T.id, "c", T.label, "foo")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must start with label prefix")
                .hasMessageContaining("foo/");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "d")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must start with label prefix")
                .hasMessageContaining("<label>/");
        assertThat(catchThrowable(() -> graph.addVertex(T.label, "foo/bar")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foo/bar")
                .hasMessageContaining("invalid character '/'");
        assertThat(catchThrowable(() -> graph.addVertex(T.label, "foo_bar")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foo_bar")
                .hasMessageContaining("invalid character '_'");
    }

    @Test
    public void prefix() {
        String prefix = ((ArangoDBGraph) graph).name() + "_";
        assertThat(catchThrowable(() -> graph.addVertex(T.id, prefix + "foo/a")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(prefix + "foo/a")
                .hasMessageContaining("invalid character '_'");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, prefix + "foo/b", T.label, "foo")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(prefix + "foo/b")
                .hasMessageContaining("invalid character '_'");
    }
}
