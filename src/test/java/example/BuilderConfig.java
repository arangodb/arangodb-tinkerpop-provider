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

package example;

import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;

public class BuilderConfig {

    public static void main(String[] args) throws Exception {
        Configuration config = new ArangoDBConfigurationBuilder()
                .graphClass(ArangoDBGraph.class)
                .db("example")
                .name("builderConfigGraph")
                .enableDataDefinition(true)
                .user("root")
                .password("test")
                .hosts("172.28.0.1:8529", "172.28.0.1:8539", "172.28.0.1:8549")
                .build();

        Graph graph = GraphFactory.open(config);
        graph.variables().set("author", "Joe");
        System.out.println(graph.variables().asMap());
        graph.close();
    }

}
