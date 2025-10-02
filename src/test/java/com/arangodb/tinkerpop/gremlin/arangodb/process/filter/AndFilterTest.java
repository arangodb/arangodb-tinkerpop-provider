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
import org.apache.tinkerpop.gremlin.process.traversal.TextP;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AndFilterTest extends AbstractGremlinTest {

    @Test
    public void testEmpty() {
        ArangoFilter filter = AndFilter.of(Arrays.asList(
                new CompareEqFilter("a", "str"),
                EmptyFilter.instance()
        ));
        assertThat(filter.getSupport()).isEqualTo(FilterSupport.FULL);
        assertThat(filter.toAql("d")).isEqualTo("`d`.`a` == \"str\"");
    }

    @Test
    public void equalToStringFilter() {
        ArangoFilter filter = AndFilter.of(Arrays.asList(
                new CompareEqFilter("a", "str"),
                new CompareEqFilter("b", 11)
        ));
        assertThat(filter.getSupport()).isEqualTo(FilterSupport.FULL);
        assertThat(filter.toAql("d")).isEqualTo("(`d`.`a` == \"str\" AND `d`.`b` == 11)");
    }

    @Test
    public void graphTraversalAndFilter() {
        graph.addVertex().property("name", "foo");
        graph.addVertex().property("name", "fee");
        List<String> res = graph.traversal().V()
                .has("name", TextP.startingWith("f").and(TextP.endingWith("o")))
                .<String>values("name").toList();
        assertThat(res)
                .hasSize(1)
                .containsExactly("foo");
    }
}