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

package com.arangodb.tinkerpop.gremlin.persistence;

import java.util.Objects;

public class VariablesData extends PropertiesContainer<Object> {

    private final String key;
    private String version;

    public VariablesData(String key, String version) {
        this.key = key;
        this.version = version;
    }

    public String getKey() {
        return key;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "VariablesData{" +
                "key='" + key + '\'' +
                ", version='" + version + '\'' +
                ", super=" + super.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VariablesData)) return false;
        if (!super.equals(o)) return false;
        VariablesData that = (VariablesData) o;
        return Objects.equals(key, that.key) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), key, version);
    }
}
