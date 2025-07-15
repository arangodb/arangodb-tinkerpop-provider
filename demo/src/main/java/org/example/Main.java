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

package org.example;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class Main {
    private static final String DB_NAME = "demo";

    public static void main(String[] args) {

        //region cleanup data from previous runs
        ArangoDB adb = new ArangoDB.Builder()
                .host("127.0.0.1", 8529)
                .user("root")
                .password("test")
                .build();
        ArangoDatabase db = adb.db(DB_NAME);
        if (db.exists()) {
            db.drop();
        }
        db.create();
        adb.shutdown();
        //endregion

        // create Tinkerpop Graph backed by ArangoDB
        Configuration conf = new ArangoDBConfigurationBuilder()
                .hosts("127.0.0.1:8529")
                .user("root")
                .password("test")
                .database(DB_NAME)
                .build();
        ArangoDBGraph g = ArangoDBGraph.open(conf);

        // print supported features
        System.out.println(g.features());

        // write vertex
        Vertex v = g.addVertex("person");
        v.property("name", "Joe");
        v.property("age", 22);

        // read vertex
        Vertex joe = g.traversal().V().has("name", "Joe").next();
        System.out.println("Joe's age: " + joe.property("age").value());

        g.close();
    }
}