package com.arangodb.tinkerpop.gremlin.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Fields {
    public static final String ID = "_id";
    public static final String KEY = "_key";
    public static final String REV = "_rev";
    public static final String FROM = "_from";
    public static final String TO = "_to";
    public static final String LABEL = "_label";
    public static final String META = "_meta";
    public static final String VERSION = "_version";

    private static final Set<String> ALL = new HashSet<>(Arrays.asList(
            ID, KEY, REV, FROM, TO, LABEL, META, VERSION
    ));

    public static boolean isReserved(String key) {
        return ALL.contains(key);
    }

    private Fields() {
    }
}
