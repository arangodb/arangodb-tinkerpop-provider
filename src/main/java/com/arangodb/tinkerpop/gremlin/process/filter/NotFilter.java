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

public class NotFilter implements ArangoFilter {
    private final ArangoFilter filter;

    public static ArangoFilter of(ArangoFilter filter) {
        if (filter.getSupport() != FilterSupport.FULL) {
            return EmptyFilter.INSTANCE;
        }
        return new NotFilter(filter);
    }

    private NotFilter(ArangoFilter filter) {
        this.filter = filter;
    }

    /**
     * +---------++---------+
     * |   v     || NOT(v)  |
     * +---------++---------+
     * | FULL    || FULL    |
     * | PARTIAL || NONE    |
     * | NONE    || NONE    |
     * +---------++---------+
     */
    @Override
    public FilterSupport getSupport() {
        return FilterSupport.FULL;
    }

    @Override
    public String toAql(String variableName) {
        return "NOT(" + filter.toAql(variableName) + ")";
    }
}
