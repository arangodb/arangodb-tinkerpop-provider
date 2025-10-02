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

public class DoubleValue implements Value {
    private final Double value;

    DoubleValue(Double value) {
        this.value = value;
    }

    @Override
    public Double value() {
        return value;
    }

    @Override
    public FilterSupport getSupport() {
        return FilterSupport.FULL;
    }
}
