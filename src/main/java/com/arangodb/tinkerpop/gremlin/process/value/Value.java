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

import com.arangodb.shaded.fasterxml.jackson.core.JsonProcessingException;
import com.arangodb.tinkerpop.gremlin.process.filter.FilterSupport;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil;
import org.apache.tinkerpop.gremlin.structure.Property;

import java.util.Collection;
import java.util.Map;

import static com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil.MAPPER;

public interface Value {

    Object value();

    FilterSupport getSupport();

    default String toAql() {
        try {
            return MAPPER.writeValueAsString(value());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static Value of(Object value) {
        if (value == null) {
            return NullValue.instance();
        } else if (value instanceof Boolean) {
            return new BooleanValue((Boolean) value);
        } else if (value instanceof Integer) {
            return new IntegerValue((Integer) value);
        } else if (value instanceof Double) {
            return new DoubleValue((Double) value);
        } else if (value instanceof Long) {
            return new LongValue((Long) value);
        } else if (value instanceof String) {
            return of((String) value);
        } else if (value instanceof Map) {
            return new MapValue((Map<?, ?>) value);
        } else if (value instanceof Collection) {
            return of((Collection<?>) value);
        } else if (value.getClass().isArray()) {
            return new ArrayValue(ArangoDBUtil.arrayToList(value));
        } else {
            throw Property.Exceptions.dataTypeOfPropertyValueNotSupported(value);
        }
    }

    static StringValue of(String value) {
        return new StringValue(value);
    }

    static ArrayValue of(Collection<?> value) {
        return new ArrayValue(value);
    }

}
