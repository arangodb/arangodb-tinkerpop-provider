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
