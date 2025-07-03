package com.arangodb.tinkerpop.gremlin;

public class TestUtils {
    private final static String SKIP_PROCESS_STANDARD_SUITE = "test.skipProcessStandardSuite";

    public static boolean skipProcessStandardSuite() {
        String p = System.getProperty(SKIP_PROCESS_STANDARD_SUITE);
        if (p == null) {
            return false;
        }
        if (p.trim().isEmpty()) {
            return true;
        }
        return Boolean.parseBoolean(p);
    }
}
