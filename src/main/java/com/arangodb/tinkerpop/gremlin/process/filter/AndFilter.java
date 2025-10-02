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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AndFilter implements ArangoFilter {
    private final List<ArangoFilter> filters;

    public static ArangoFilter of(Collection<ArangoFilter> filters) {
        List<ArangoFilter> supportedFilters = filters.stream()
                .filter(it -> it.getSupport() != FilterSupport.NONE)
                .collect(Collectors.toList());
        if (supportedFilters.isEmpty()) {
            return EmptyFilter.instance();
        } else if (supportedFilters.size() == 1) {
            return supportedFilters.get(0);
        } else {
            return new AndFilter(supportedFilters);
        }
    }

    private AndFilter(List<ArangoFilter> filters) {
        this.filters = filters;
    }

    /**
     * +---------++---------+---------+---------+
     * |   AND   ||  FULL   | PARTIAL |  NONE   |
     * +---------++---------+---------+---------+
     * | FULL    || FULL    | PARTIAL | PARTIAL |
     * | PARTIAL || PARTIAL | PARTIAL | PARTIAL |
     * | NONE    || PARTIAL | PARTIAL | NONE    |
     * +---------++---------+---------+---------+
     */
    @Override
    public FilterSupport getSupport() {
        if (filters.stream().map(ArangoFilter::getSupport).allMatch(FilterSupport.FULL::equals)) {
            return FilterSupport.FULL;
        } else {
            return FilterSupport.PARTIAL;
        }
    }

    @Override
    public String toAql(String variableName) {
        return filters.stream()
                .map(it -> it.toAql(variableName))
                .collect(Collectors.joining(" AND ", "(", ")"));
    }
}
