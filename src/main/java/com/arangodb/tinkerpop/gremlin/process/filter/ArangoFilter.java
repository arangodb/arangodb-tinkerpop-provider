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

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.util.AndP;
import org.apache.tinkerpop.gremlin.process.traversal.util.OrP;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public interface ArangoFilter {
    static ArangoFilter of(String key, P<?> p) {
        String pName = p.getPredicateName();
        switch (pName) {
            case "eq":
                return new CompareEqFilter(key, p.getValue());
            case "neq":
                return NotFilter.of(new CompareEqFilter(key, p.getValue()));
            case "lt":
                return new CompareLtFilter(key, p.getValue());
            case "lte":
                return OrFilter.of(Arrays.asList(new CompareLtFilter(key, p.getValue()), new CompareEqFilter(key, p.getValue())));
            case "gt":
                return new CompareGtFilter(key, p.getValue());
            case "gte":
                return OrFilter.of(Arrays.asList(new CompareGtFilter(key, p.getValue()), new CompareEqFilter(key, p.getValue())));
            case "within":
                return new ContainsWithinFilter(key, (Collection<?>) p.getValue());
            case "without":
                return NotFilter.of(new ContainsWithinFilter(key, (Collection<?>) p.getValue()));
            case "containing":
                return new TextContainingFilter(key, (String) p.getValue());
            case "notContaining":
                return NotFilter.of(new TextContainingFilter(key, (String) p.getValue()));
            case "endingWith":
                return new TextRegexFilter(key, p.getValue() + "$");
            case "notEndingWith":
                return NotFilter.of(new TextRegexFilter(key, p.getValue() + "$"));
            case "startingWith":
                return new TextStartingWithFilter(key, (String) p.getValue());
            case "notStartingWith":
                return NotFilter.of(new TextStartingWithFilter(key, (String) p.getValue()));
            case "regex":
                return new TextRegexFilter(key, (String) p.getValue());
            case "notRegex":
                return NotFilter.of(new TextRegexFilter(key, (String) p.getValue()));
            case "or":
                if (p instanceof OrP) {
                    return OrFilter.of(((OrP<?>) p).getPredicates().stream()
                            .map(it -> of(key, it))
                            .collect(Collectors.toList()));
                }
                throw new UnsupportedOperationException("Unsupported predicate: " + p);
            case "and":
                if (p instanceof AndP) {
                    return AndFilter.of(((AndP<?>) p).getPredicates().stream()
                            .map(it -> of(key, it))
                            .collect(Collectors.toList()));
                }
                throw new UnsupportedOperationException("Unsupported predicate: " + p);
            default:
                throw new UnsupportedOperationException("Unsupported predicate: " + p);
        }
    }

    FilterSupport getSupport();

    String toAql(String variableName);
}
