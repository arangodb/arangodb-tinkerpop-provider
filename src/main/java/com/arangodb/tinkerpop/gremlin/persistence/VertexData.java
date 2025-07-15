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

package com.arangodb.tinkerpop.gremlin.persistence;

import java.util.*;

public class VertexData extends PropertiesContainer<VertexPropertyData> implements PersistentData {

    private final ElementId id;
    private final String label;

    public VertexData(String label, ElementId id) {
        this.id = id;
        this.label = label;
    }

    @Override
    public ElementId elementId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "VertexData{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", super=" + super.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VertexData)) return false;
        if (!super.equals(o)) return false;
        VertexData that = (VertexData) o;
        return Objects.equals(id, that.id) && Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, label);
    }
}
