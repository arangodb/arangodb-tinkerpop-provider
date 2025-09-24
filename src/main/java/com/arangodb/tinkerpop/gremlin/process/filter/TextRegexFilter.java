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

package com.arangodb.tinkerpop.gremlin.process.filter;


import com.arangodb.tinkerpop.gremlin.process.value.StringValue;
import com.arangodb.tinkerpop.gremlin.process.value.Value;

import java.util.Objects;

public class TextRegexFilter implements ArangoFilter {

    private final String attribute;
    private final StringValue value;

    public TextRegexFilter(String attribute, String value) {
        Objects.requireNonNull(attribute, "attribute cannot be null");
        if (attribute.isEmpty()) {
            throw new IllegalArgumentException("attribute cannot be empty");
        }
        this.attribute = attribute;
        this.value = Value.of(value);
    }

    @Override
    public FilterSupport getSupport() {
        return FilterSupport.FULL;
    }

    @Override
    public String toAql(String variableName) {
        return "REGEX_TEST(`" + variableName + "`.`" + attribute + "`, " + value.toAql() + ")";
    }
}
