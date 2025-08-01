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

import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class SimpleElementIdTest extends AbstractGremlinTest {

    @Test
    public void id() {
        assertThat(graph.addVertex(T.id, "a").id()).isEqualTo("a");
        assertThat(graph.addVertex(T.id, "b", T.label, "bar").id()).isEqualTo("b");
        assertThat(graph.addVertex().id())
                .isInstanceOf(String.class)
                .asString()
                .doesNotContain("/");

        assertThat(catchThrowable(() -> graph.addVertex(T.id, "foo/bar")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foo/bar")
                .hasMessageContaining("invalid character '/'");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "foo_bar")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foo_bar")
                .hasMessageContaining("invalid character '_'");
    }

    @Test
    public void label() {
        assertThat(graph.addVertex(T.label, "foo").label()).isEqualTo("foo");
        assertThat(graph.addVertex(T.id, "a", T.label, "bar").label()).isEqualTo("bar");
        assertThat(graph.addVertex(T.id, "b").label()).isEqualTo(Vertex.DEFAULT_LABEL);
        assertThat(graph.addVertex().label()).isEqualTo(Vertex.DEFAULT_LABEL);
    }
}
