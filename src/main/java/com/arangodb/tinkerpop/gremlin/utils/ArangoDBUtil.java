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

import com.arangodb.entity.GraphEntity;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import com.arangodb.tinkerpop.gremlin.PackageVersion;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.GraphVariableHelper;

public class ArangoDBUtil {

    private ArangoDBUtil() {
    }

    public static void checkExistingGraph(GraphEntity info, ArangoDBGraphConfig config) {
        // check orphanCollections
        if (!CollectionUtils.isEqualCollection(info.getOrphanCollections(), config.orphanCollections)) {
            throw new IllegalStateException("Orphan collections do not match. From DB: "
                    + info.getOrphanCollections() + ", From config: " + config.orphanCollections);
        }

        // check edgeDefinitions
        Set<ArangoDBGraphConfig.EdgeDef> dbDefs = info.getEdgeDefinitions().stream()
                .map(ArangoDBGraphConfig.EdgeDef::of)
                .collect(Collectors.toSet());
        if (!dbDefs.equals(config.edgeDefinitions)) {
            throw new IllegalStateException("Edge definitions do not match. From DB: "
                    + dbDefs + ", From config: " + config.edgeDefinitions);
        }
    }

    public static void validateProperty(final String key, final Object value) {
        ElementHelper.validateProperty(key, value);
        if (Fields.isReserved(key)) {
            throw new IllegalArgumentException("Property key can not be a reserved key: " + key);
        }
        if (!supportsDataType(value)) {
            throw Property.Exceptions.dataTypeOfPropertyValueNotSupported(value);
        }
    }

    public static void validateVariable(String key, Object value) {
        GraphVariableHelper.validateVariable(key, value);
        if (Fields.isReserved(key)) {
            throw new IllegalArgumentException("Graph variable key can not be a reserved key: " + key);
        }
        if (!supportsDataType(value)) {
            throw Graph.Variables.Exceptions.dataTypeOfVariableValueNotSupported(value);
        }
    }

    private static boolean supportsDataType(Object value) {
        return value == null ||
                value instanceof Boolean || value instanceof boolean[] ||
                value instanceof Double || value instanceof double[] ||
                value instanceof Integer || value instanceof int[] ||
                value instanceof Long || value instanceof long[] ||
                value instanceof String || value instanceof String[];
    }

    public static void checkVersion(String version) {
        if (new VersionComparator().compare(version, PackageVersion.VERSION) > 0) {
            throw new IllegalStateException("Existing graph has more recent version [" + version +
                    "] than library version [" + PackageVersion.VERSION + "].");
        }
    }

    private static class VersionComparator implements Comparator<String>, Serializable {
        @Override
        public int compare(String a, String b) {
            Objects.requireNonNull(a);
            Objects.requireNonNull(b);
            Pattern versionPattern = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*).*");

            Matcher ma = versionPattern.matcher(a);
            if (!ma.matches()) {
                throw new IllegalArgumentException("Invalid version: " + a);
            }
            int aMajor = Integer.parseInt(ma.group(1));
            int aMinor = Integer.parseInt(ma.group(2));
            int aPatch = Integer.parseInt(ma.group(3));

            Matcher mb = versionPattern.matcher(b);
            if (!mb.matches()) {
                throw new IllegalArgumentException("Invalid version: " + b);
            }
            int bMajor = Integer.parseInt(mb.group(1));
            int bMinor = Integer.parseInt(mb.group(2));
            int bPatch = Integer.parseInt(mb.group(3));

            if (aMajor < bMajor) {
                return -1;
            }
            if (aMajor > bMajor) {
                return 1;
            }

            if (aMinor < bMinor) {
                return -1;
            }
            if (aMinor > bMinor) {
                return 1;
            }

            return Integer.compare(aPatch, bPatch);
        }
    }

}
