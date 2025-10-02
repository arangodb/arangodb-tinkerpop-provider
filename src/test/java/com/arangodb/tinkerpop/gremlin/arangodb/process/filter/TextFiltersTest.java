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

package com.arangodb.tinkerpop.gremlin.arangodb.process.filter;

import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.*;

import static org.apache.tinkerpop.gremlin.process.traversal.TextP.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TextFiltersTest extends AbstractGremlinTest {

    @Test
    public void textStartingWithFilter() {
        graph.addVertex().property("value", "foo");
        graph.addVertex().property("value", "bar");
        List<Vertex> res = graph.traversal().V().has("value", startingWith("fo")).toList();
        assertThat(res)
                .hasSize(1)
                .map(v -> v.value("value"))
                .first()
                .isEqualTo("foo");
    }

    @Test
    public void textNotStartingWithFilter() {
        graph.addVertex().property("value", "foo");
        graph.addVertex().property("value", "bar");
        List<Vertex> res = graph.traversal().V().has("value", notStartingWith("fo")).toList();
        assertThat(res)
                .hasSize(1)
                .map(v -> v.value("value"))
                .first()
                .isEqualTo("bar");
    }

    @Test
    public void textEndingWithFilter() {
        graph.addVertex().property("value", "foo\\");
        graph.addVertex().property("value", "bar");
        List<Vertex> res = graph.traversal().V().has("value", endingWith("oo\\")).toList();
        assertThat(res)
                .hasSize(1)
                .map(v -> v.value("value"))
                .first()
                .isEqualTo("foo\\");
    }

    @Test
    public void textNotEndingWithFilter() {
        graph.addVertex().property("value", "foo");
        graph.addVertex().property("value", "bar");
        List<Vertex> res = graph.traversal().V().has("value", notEndingWith("oo")).toList();
        assertThat(res)
                .hasSize(1)
                .map(v -> v.value("value"))
                .first()
                .isEqualTo("bar");
    }

    @Test
    public void textContainingFilter() {
        graph.addVertex().property("value", "foo");
        graph.addVertex().property("value", "bar");
        List<Vertex> res = graph.traversal().V().has("value", containing("a")).toList();
        assertThat(res)
                .hasSize(1)
                .map(v -> v.value("value"))
                .first()
                .isEqualTo("bar");
    }

    @Test
    public void textNotContainingFilter() {
        graph.addVertex().property("value", "foo");
        graph.addVertex().property("value", "bar");
        List<Vertex> res = graph.traversal().V().has("value", notContaining("a")).toList();
        assertThat(res)
                .hasSize(1)
                .map(v -> v.value("value"))
                .first()
                .isEqualTo("foo");
    }

    @Test
    public void textRegexFilter() {
        graph.addVertex().property("value", "foo");
        graph.addVertex().property("value", "bar");
        List<Vertex> res = graph.traversal().V().has("value", regex("^b.*r$")).toList();
        assertThat(res)
                .hasSize(1)
                .map(v -> v.value("value"))
                .first()
                .isEqualTo("bar");
    }

    @Test
    public void textNotRegexFilter() {
        graph.addVertex().property("value", "foo");
        graph.addVertex().property("value", "bar");
        List<Vertex> res = graph.traversal().V().has("value", notRegex("^b.*r$")).toList();
        assertThat(res)
                .hasSize(1)
                .map(v -> v.value("value"))
                .first()
                .isEqualTo("foo");
    }

}
