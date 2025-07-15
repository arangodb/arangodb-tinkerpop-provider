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

package com.arangodb.tinkerpop.gremlin.jsr223;

import com.arangodb.tinkerpop.gremlin.persistence.*;
import org.apache.tinkerpop.gremlin.jsr223.AbstractGremlinPlugin;
import org.apache.tinkerpop.gremlin.jsr223.DefaultImportCustomizer;
import org.apache.tinkerpop.gremlin.jsr223.ImportCustomizer;

import com.arangodb.tinkerpop.gremlin.client.*;
import com.arangodb.tinkerpop.gremlin.structure.*;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil;

public class ArangoDBGremlinPlugin extends AbstractGremlinPlugin {

    private static final String NAME = "tinkerpop.arangodb";
    private static final ImportCustomizer IMPORTS;

    static {
        try {
            IMPORTS = DefaultImportCustomizer.build().addClassImports(
                            ArangoDBGraphClient.class,
                            ArangoDBQueryBuilder.class,
                            ArangoDBUtil.class,

                            // structure
                            ArangoDBEdge.class,
                            ArangoDBElement.class,
                            ArangoDBGraph.class,
                            ArangoDBGraphVariables.class,
                            ArangoDBPersistentElement.class,
                            ArangoDBProperty.class,
                            ArangoDBSimpleElement.class,
                            ArangoDBVertex.class,
                            ArangoDBVertexProperty.class,

                            // persistence
                            EdgeData.class,
                            PersistentData.class,
                            PropertiesContainer.class,
                            VariablesData.class,
                            VertexData.class,
                            VertexPropertyData.class
                    )
                    .create();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final ArangoDBGremlinPlugin INSTANCE = new ArangoDBGremlinPlugin();

    public ArangoDBGremlinPlugin() {
        super(NAME, IMPORTS);
    }

    public static ArangoDBGremlinPlugin instance() {
        return INSTANCE;
    }

    @Override
    public boolean requireRestart() {
        return true;
    }

}
