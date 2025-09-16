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

package com.arangodb.tinkerpop.gremlin.structure;


import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.arangodb.tinkerpop.gremlin.PackageVersion;
import com.arangodb.tinkerpop.gremlin.persistence.VariablesData;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;


public class ArangoDBGraphVariables implements Graph.Variables {

    private final ArangoDBGraph graph;
    private final VariablesData data;

    ArangoDBGraphVariables(ArangoDBGraph graph, VariablesData data) {
        this.graph = graph;
        this.data = data;
    }

    public String getVersion() {
        return data.getVersion();
    }

    void updateVersion() {
        ArangoDBUtil.checkVersion(getVersion());
        data.setVersion(PackageVersion.VERSION);
        update();
    }

    @Override
    public Set<String> keys() {
        return data.keySet().stream()
                .filter(it -> !Graph.Hidden.isHidden(it))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> Optional<R> get(String key) {
        return Optional.ofNullable((R) data.get(key));
    }

    @Override
    public void set(String key, Object value) {
        ArangoDBUtil.validateVariable(key, value, graph.config);
        data.put(key, value);
        update();
    }

    @Override
    public void remove(String key) {
        data.remove(key);
        update();
    }

    private void update() {
        graph.getClient().updateGraphVariables(data);
    }

    @Override
    public String toString() {
        return StringFactory.graphVariablesString(this);
    }

}
