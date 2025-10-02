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

package com.arangodb.tinkerpop.gremlin.process.traversal.step;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.StartStep;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.util.Iterator;


public final class AQLStartStep extends StartStep<Object> {

    private final String query;

    public AQLStartStep(final Traversal.Admin traversal, final String query, final Iterator<?> aqlCursor) {
        super(traversal, aqlCursor);
        this.query = query;
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this, this.query);
    }
}
