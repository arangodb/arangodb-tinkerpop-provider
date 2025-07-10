/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.arangodb.tinkerpop.gremlin.persistence;

import java.util.*;

public class EdgeData extends PropertiesContainer<Object> implements PersistentData {

    private final ElementId id;
    private final String label;
    private final ElementId from;
    private final ElementId to;

    public EdgeData(String label, ElementId id, ElementId from, ElementId to) {
        this.id = id;
        this.label = label != null ? label : id.getLabel();
        this.from = from;
        this.to = to;
    }

    @Override
    public ElementId elementId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public ElementId getFrom() {
        return from;
    }

    public ElementId getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "EdgeData{" +
                "from=" + from +
                ", id=" + id +
                ", label='" + label + '\'' +
                ", to=" + to +
                ", super=" + super.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EdgeData)) return false;
        if (!super.equals(o)) return false;
        EdgeData edgeData = (EdgeData) o;
        return Objects.equals(id, edgeData.id) && Objects.equals(label, edgeData.label) && Objects.equals(from, edgeData.from) && Objects.equals(to, edgeData.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, label, from, to);
    }
}
