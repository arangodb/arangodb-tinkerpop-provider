package com.arangodb.tinkerpop.gremlin.complex;

import org.apache.tinkerpop.gremlin.GraphProviderClass;
import org.apache.tinkerpop.gremlin.process.ProcessStandardSuite;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import static com.arangodb.tinkerpop.gremlin.TestUtils.skipProcessStandardSuite;
import static org.junit.Assume.assumeTrue;

@RunWith(ProcessStandardSuite.class)
@GraphProviderClass(provider = ComplexGraphProvider.class, graph = ComplexTestGraph.class)
public class ComplexProcessStandardSuiteTest {

    @BeforeClass
    public static void setup() {
        assumeTrue(!skipProcessStandardSuite());
    }

}
