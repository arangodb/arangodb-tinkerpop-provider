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

package com.arangodb.tinkerpop.gremlin.process.value;

import com.arangodb.tinkerpop.gremlin.process.filter.FilterSupport;

import java.util.Map;
import java.util.stream.Collectors;

public class MapValue implements Value {
    private final Map<String, Value> values;

    MapValue(Map<?, ?> values) {
        this.values = values.entrySet().stream()
                .peek(it -> {
                    if (!(it.getKey() instanceof String)) {
                        throw new IllegalArgumentException("key must be a string");
                    }
                })
                .collect(Collectors.toMap(
                        e -> (String) e.getKey(),
                        e -> Value.of(e.getValue())
                ));
    }

    @Override
    public Map<String, ?> value() {
        return values.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().value()
                ));
    }

    @Override
    public FilterSupport getSupport() {
        if (values.values().stream().map(Value::getSupport).anyMatch(FilterSupport.NONE::equals)) {
            return FilterSupport.NONE;
        } else if (values.values().stream().map(Value::getSupport).anyMatch(FilterSupport.PARTIAL::equals)) {
            return FilterSupport.PARTIAL;
        } else {
            return FilterSupport.FULL;
        }
    }

    @Override
    public String toAql() {
        return values.entrySet().stream()
                .map(it -> "\"" + it.getKey() + "\": " + it.getValue().toAql())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
