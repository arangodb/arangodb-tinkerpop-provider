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
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil.normalizeValue;
import static com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil.supportsDataType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class DataTypesTest extends AbstractGremlinTest {

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
    public void checkTestDataSupported() {
        for (Object value : data) {
            if (!supportsDataType(value)) {
                throw Property.Exceptions.dataTypeOfPropertyValueNotSupported(value);
            }
        }
    }

    @Test
    public void variables() {
        data.forEach(this::testVariables);
        unsupportedData.forEach(this::testUnsupportedVariables);
    }

    private void testVariables(Object value) {
        if (value == null) return;
        graph.variables().set("value", value);
        Optional<Object> got = graph.variables().get("value");
        assertThat(got).isPresent().get().isEqualTo(normalizeValue(value));
    }

    private void testUnsupportedVariables(Object value) {
        Throwable thrown = catchThrowable(() -> graph.variables().set("value", value));
        assertThat(thrown)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Graph variable value [%s] is of type %s is not supported", value, value.getClass());
    }

    @Test
    public void vertexProperties() {
        data.forEach(this::testVertexProperties);
        unsupportedData.forEach(this::testUnsupportedVertexProperties);
        unsupportedData.forEach(this::testUnsupportedMetaProperties);
    }

    private void testVertexProperties(Object value) {
        Vertex v = graph.addVertex();
        v
                .property("value", value)  // set vertex property value
                .property("meta", value);  // set meta property value
        VertexProperty<Object> p = graph.vertices(v.id()).next().property("value");
        assertThat(p.value()).isEqualTo(normalizeValue(value));
        Property<Object> meta = p.property("meta");
        assertThat(meta.isPresent()).isTrue();
        assertThat(meta.value()).isEqualTo(normalizeValue(value));
    }

    private void testUnsupportedVertexProperties(Object value) {
        Throwable thrown = catchThrowable(() -> graph.addVertex("value", value));
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Property value [%s] is of type %s is not supported", value, value.getClass());
    }

    private void testUnsupportedMetaProperties(Object value) {
        VertexProperty<String> p = graph.addVertex().property("value", "ok");
        Throwable thrown = catchThrowable(() -> p.property("value", value));
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Property value [%s] is of type %s is not supported", value, value.getClass());
    }

    @Test
    public void edgeProperties() {
        data.forEach(this::testEdgeProperties);
        unsupportedData.forEach(this::testUnsupportedEdgeProperties);
    }

    private void testEdgeProperties(Object value) {
        Vertex a = graph.addVertex();
        Vertex b = graph.addVertex();
        Edge e = a.addEdge("edge", b, "value", value);
        Property<Object> p = graph.edges(e.id()).next().properties("value").next();
        assertThat(p.isPresent()).isTrue();
        assertThat(p.value()).isEqualTo(normalizeValue(value));
    }

    private void testUnsupportedEdgeProperties(Object value) {
        Vertex a = graph.addVertex();
        Vertex b = graph.addVertex();
        Throwable thrown = catchThrowable(() -> a.addEdge("edge", b, "value", value));
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Property value [%s] is of type %s is not supported", value, value.getClass());
    }
}
