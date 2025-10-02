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

import com.arangodb.tinkerpop.gremlin.process.filter.CompareEqFilter;
import com.arangodb.tinkerpop.gremlin.process.filter.FilterSupport;
import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil.normalizeValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class CompareEqFilterTest extends AbstractGremlinTest {

    private final List<?> data = Arrays.asList(
            null,
            Boolean.TRUE,
            Boolean.FALSE,
            new boolean[]{true, false, true},
            12.12d,
            new double[]{1.1d, 2.2d, 3.3d},
            11,
            new int[]{1, 2, 3, 4, 5},
            5_000_000_000L,
            new long[]{5_000_000_000L, 6_000_000_000L, 7_000_000_000L},
            "hello",
            new String[]{"hello", "world", "test"},
            Arrays.asList(null, true, 2.2d, 22, 5_000_000_000L, "hello"),
            new HashMap<String, Object>() {{
                put("k1", null);
                put("k2", true);
                put("k3", 2.2d);
                put("k4", 22);
                put("k5", 5_000_000_000L);
                put("k6", "hello");
            }}
    );

    private final List<?> unsupportedData = Arrays.asList(
            new Date(),
            new java.sql.Date(new Date().getTime()),
            new Date[]{new Date()},
            UUID.randomUUID(),
            BigInteger.TEN,
            BigDecimal.ONE,
            Float.NEGATIVE_INFINITY,
            Float.POSITIVE_INFINITY,
            1.23f,
            (byte) 0x22,
            (short) 11,
            Void.class
    );

    @Test
    public void equalToStringFilter() {
        CompareEqFilter filter = new CompareEqFilter("field", "str");
        assertThat(filter.getSupport()).isEqualTo(FilterSupport.FULL);
        assertThat(filter.toAql("d")).isEqualTo("`d`.`field` == \"str\"");
    }

    @Test
    public void equalToBoolFilter() {
        CompareEqFilter filter = new CompareEqFilter("field", false);
        assertThat(filter.getSupport()).isEqualTo(FilterSupport.FULL);
        assertThat(filter.toAql("d")).isEqualTo("`d`.`field` == false");
    }

    @Test
    public void equalToIntegerFilter() {
        CompareEqFilter filter = new CompareEqFilter("field", 22);
        assertThat(filter.getSupport()).isEqualTo(FilterSupport.FULL);
        assertThat(filter.toAql("d")).isEqualTo("`d`.`field` == 22");
    }

    @Test
    public void equalToNullFilter() {
        CompareEqFilter filter = new CompareEqFilter("field", null);
        assertThat(filter.getSupport()).isEqualTo(FilterSupport.PARTIAL);
        assertThat(filter.toAql("d")).isEqualTo("`d`.`field` == null");
    }

    @Test
    public void graphTraversalFilterValues() {
        data.forEach(this::testGraphTraversalFilterValues);
        unsupportedData.forEach(this::testUnsupportedGraphTraversalFilterValues);
    }

    @Test
    public void idEqualToStringFilter() {
        Vertex v = graph.addVertex();
        v.property("value", "foo");
        List<Vertex> res = graph.traversal().V().hasId(v.id()).has("value", "foo").toList();
        assertThat(res)
                .hasSize(1)
                .allMatch(vertex -> vertex.id().equals(v.id()));
    }

    @Test
    public void labelEqualToStringFilter() {
        graph.addVertex("vertex").property("value", "foo");
        graph.addVertex("foo").property("value", "foo");

        List<Vertex> res = graph.traversal().V().hasLabel("foo").has("value", "foo").toList();
        assertThat(res)
                .hasSize(1)
                .allMatch(vertex -> vertex.label().equals("foo"));
    }

    private void testGraphTraversalFilterValues(Object value) {
        Vertex v = graph.addVertex();
        v.property("value", value);
        Object p = graph.traversal().V().has("value", value).values("value").next();
        assertThat(p).isEqualTo(normalizeValue(value));
    }

    private void testUnsupportedGraphTraversalFilterValues(Object value) {
        Throwable thrown = catchThrowable(() -> graph.traversal().V().has("value", value).iterate());
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Property value")
                .hasMessageContaining("of type")
                .hasMessageContaining("is not supported");
    }
}
