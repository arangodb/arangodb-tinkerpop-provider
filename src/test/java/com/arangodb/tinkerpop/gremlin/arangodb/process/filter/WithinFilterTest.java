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

import com.arangodb.tinkerpop.gremlin.process.filter.*;
import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WithinFilterTest extends AbstractGremlinTest {

    @Test
    public void withinStringFilter() {
        ArangoFilter filter = new ContainsWithinFilter("a", Arrays.asList("str", 11));
        assertThat(filter.getSupport()).isEqualTo(FilterSupport.FULL);
        assertThat(filter.toAql("d")).isEqualTo("`d`.`a` IN [\"str\", 11]");
    }

    @Test
    public void withinNullFilter() {
        ArangoFilter filter = new ContainsWithinFilter("a", Arrays.asList("str", null));
        assertThat(filter.getSupport()).isEqualTo(FilterSupport.PARTIAL);
        assertThat(filter.toAql("d")).isEqualTo("`d`.`a` IN [\"str\", null]");
    }

    @Test
    public void graphTraversalWithinFilter() {
        graph.addVertex().property("name", "foo");
        graph.addVertex().property("name", "bar");
        List<String> res = graph.traversal().V()
                .has("name", P.within("foo", "bar"))
                .<String>values("name").toList();
        assertThat(res)
                .hasSize(2)
                .containsExactly("foo", "bar");
    }

    @Test
    public void graphTraversalWithinNullFilter() {
        graph.addVertex().property("name", "foo");
        graph.addVertex().property("name", null);
        List<Object> res = graph.traversal().V()
                .has("name", P.within("foo", null))
                .values("name").toList();
        assertThat(res)
                .hasSize(2)
                .containsExactly("foo", null);
    }

    @Test
    public void graphTraversalWithoutFilter() {
        graph.addVertex().property("name", "foo");
        graph.addVertex().property("name", "bar");
        graph.addVertex().property("name", "baz");
        List<String> res = graph.traversal().V()
                .has("name", P.without("foo", "bar"))
                .<String>values("name").toList();
        assertThat(res)
                .hasSize(1)
                .containsExactly("baz");
    }

    @Test
    public void graphTraversalWithoutNullFilter() {
        graph.addVertex().property("name", "foo");
        graph.addVertex().property("name", null);
        graph.addVertex().property("name", "baz");
        List<Object> res = graph.traversal().V()
                .has("name", P.without("foo", null))
                .values("name").toList();
        assertThat(res)
                .hasSize(1)
                .containsExactly("baz");
    }
}
