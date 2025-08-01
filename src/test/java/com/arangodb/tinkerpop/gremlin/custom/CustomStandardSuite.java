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

package com.arangodb.tinkerpop.gremlin.custom;

import com.arangodb.tinkerpop.gremlin.custom.process.traversal.step.map.MergeEdgeTest;
import com.arangodb.tinkerpop.gremlin.custom.structure.util.detached.DetachedGraphTest;
import com.arangodb.tinkerpop.gremlin.custom.structure.util.star.StarGraphTest;
import org.apache.tinkerpop.gremlin.AbstractGremlinSuite;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalEngine;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;


public class CustomStandardSuite extends AbstractGremlinSuite {

    private static final Class<?>[] allTests = new Class<?>[]{
            MergeEdgeTest.Traversals.class,
            StarGraphTest.class,
            DetachedGraphTest.class
    };

    public CustomStandardSuite(final Class<?> klass, final RunnerBuilder builder) throws InitializationError {
        super(klass, builder, allTests, null, false, TraversalEngine.Type.STANDARD);
    }

}
