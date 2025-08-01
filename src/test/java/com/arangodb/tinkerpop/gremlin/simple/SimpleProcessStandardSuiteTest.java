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

package com.arangodb.tinkerpop.gremlin.simple;

import org.apache.tinkerpop.gremlin.GraphProviderClass;
import org.apache.tinkerpop.gremlin.process.ProcessStandardSuite;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import static com.arangodb.tinkerpop.gremlin.TestUtils.skipProcessStandardSuite;
import static org.junit.Assume.assumeTrue;


@RunWith(ProcessStandardSuite.class)
@GraphProviderClass(provider = SimpleGraphProvider.class, graph = SimpleTestGraph.class)
public class SimpleProcessStandardSuiteTest {

    @BeforeClass
    public static void setup() {
        assumeTrue(!skipProcessStandardSuite());
    }

}
