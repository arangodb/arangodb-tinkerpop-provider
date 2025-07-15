/*
 * Copyright 2025 ArangoDB GmbH, Cologne, Germany
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

import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("resource")
public class DriverTest extends AbstractGremlinTest {

    private ArangoDBGraph graph() {
        return (ArangoDBGraph) this.graph;
    }

    @Test
    public void shouldGetDriverVersion() {
        assertThat(graph().getArangoDriver().getVersion()).isNotNull();
    }

    @Test
    public void shouldGetDatabaseInfo() {
        assertThat(graph().getArangoDatabase().getInfo().getName())
                .isEqualTo("SimpleGraphProvider");
    }

    @Test
    public void shouldGetGraphInfo() {
        assertThat(graph().getArangoGraph().getInfo().getName())
                .isEqualTo("standard");
    }
}