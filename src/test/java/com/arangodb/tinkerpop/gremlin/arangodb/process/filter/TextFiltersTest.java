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
        graph.addVertex().property("value", "foo");
        graph.addVertex().property("value", "bar");
        List<Vertex> res = graph.traversal().V().has("value", endingWith("oo")).toList();
        assertThat(res)
                .hasSize(1)
                .map(v -> v.value("value"))
                .first()
                .isEqualTo("foo");
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
