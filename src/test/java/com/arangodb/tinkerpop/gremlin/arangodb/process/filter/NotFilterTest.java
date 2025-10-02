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

import com.arangodb.tinkerpop.gremlin.process.filter.ArangoFilter;
import com.arangodb.tinkerpop.gremlin.process.filter.CompareEqFilter;
import com.arangodb.tinkerpop.gremlin.process.filter.FilterSupport;
import com.arangodb.tinkerpop.gremlin.process.filter.NotFilter;
import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class NotFilterTest extends AbstractGremlinTest {

    @Test
    public void neqToStringFilter() {
        ArangoFilter filter = NotFilter.of(new CompareEqFilter("field", "str"));
        assertThat(filter.getSupport()).isEqualTo(FilterSupport.FULL);
        assertThat(filter.toAql("d")).isEqualTo("NOT(`d`.`field` == \"str\")");
    }

    @Test
    public void neqToBoolFilter() {
        ArangoFilter filter = NotFilter.of(new CompareEqFilter("field", false));
        assertThat(filter.getSupport()).isEqualTo(FilterSupport.FULL);
        assertThat(filter.toAql("d")).isEqualTo("NOT(`d`.`field` == false)");
    }

    @Test
    public void neqToIntegerFilter() {
        ArangoFilter filter = NotFilter.of(new CompareEqFilter("field", 22));
        assertThat(filter.getSupport()).isEqualTo(FilterSupport.FULL);
        assertThat(filter.toAql("d")).isEqualTo("NOT(`d`.`field` == 22)");
    }

    @Test
    public void neqToNullFilter() {
        ArangoFilter filter = NotFilter.of(new CompareEqFilter("field", null));
        assertThat(filter.getSupport()).isEqualTo(FilterSupport.NONE);
        Throwable thrown = catchThrowable(() -> filter.toAql("x"));
        assertThat(thrown).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void graphTraversalNeqFilter() {
        graph.addVertex().property("name", "foo");
        graph.addVertex().property("name", "bar");
        Object p = graph.traversal().V().has("name", P.neq("foo")).values("name").next();
        assertThat(p).isEqualTo("bar");
    }

    @Test
    public void graphTraversalNeqNullFilter() {
        graph.addVertex().property("name", null);
        graph.addVertex().property("name", "bar");
        graph.addVertex().property("foo", "bar");
        List<Vertex> res = graph.traversal().V().has("name", P.neq(null)).toList();
        assertThat(res)
                .hasSize(1)
                .allMatch(v -> v.property("name").isPresent())
                .map(v -> v.value("name"))
                .first()
                .isEqualTo("bar");
    }

}
