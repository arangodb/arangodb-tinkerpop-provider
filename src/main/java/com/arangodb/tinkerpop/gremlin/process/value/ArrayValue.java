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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayValue implements Value {
    private final List<Value> values;

    ArrayValue(Collection<?> values) {
        this.values = values.stream()
                .map(Value::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<?> value() {
        return values.stream()
                .map(Value::value)
                .collect(Collectors.toList());
    }

    @Override
    public FilterSupport getSupport() {
        if (values.stream().map(Value::getSupport).anyMatch(FilterSupport.NONE::equals)) {
            return FilterSupport.NONE;
        } else if (values.stream().map(Value::getSupport).anyMatch(FilterSupport.PARTIAL::equals)) {
            return FilterSupport.PARTIAL;
        } else {
            return FilterSupport.FULL;
        }
    }

    @Override
    public String toAql() {
        return values.stream()
                .map(Value::toAql)
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
