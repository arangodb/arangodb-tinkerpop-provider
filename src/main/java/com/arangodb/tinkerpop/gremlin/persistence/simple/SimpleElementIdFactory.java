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

package com.arangodb.tinkerpop.gremlin.persistence.simple;

import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.arangodb.tinkerpop.gremlin.persistence.ElementIdFactory;


public class SimpleElementIdFactory extends ElementIdFactory {

    public SimpleElementIdFactory(String prefix) {
        super(prefix);
    }

    @Override
    protected String inferCollection(final String collection, final String label, final String defaultLabel) {
        return defaultLabel;
    }

    @Override
    protected void validateId(String id) {
        if (id.contains("_")) {
            throw new IllegalArgumentException(String.format("id (%s) contains invalid character '_'", id));
        }
        if (id.contains("/")) {
            throw new IllegalArgumentException(String.format("id (%s) contains invalid character '/'", id));
        }
    }

    @Override
    protected ElementId doCreate(String prefix, String collection, String key) {
        return new SimpleId(prefix, collection, key);
    }
}
