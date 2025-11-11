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

package com.arangodb.tinkerpop.gremlin.complex;

import com.arangodb.config.HostDescription;
import com.arangodb.tinkerpop.gremlin.AbstractTest;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

public class ArangoDBGraphConfigTest extends AbstractTest {

    @Test
    public void loadConfigFromFile() {
        ArangoDBGraphConfig conf = new ArangoDBGraphConfig(getConfiguration(new File("src/test/resources/test.yaml")));
        assertThat(conf.dbName).isEqualTo(dbName);
        assertThat(conf.graphName).isEqualTo("g");
        assertThat(conf.graphType).isEqualTo(ArangoDBGraphConfig.GraphType.COMPLEX);
        assertThat(conf.orphanCollections).containsExactlyInAnyOrder("x", "y", "z");
        assertThat(conf.edgeDefinitions).hasSize(2)
                .anySatisfy(e -> {
                    assertThat(e.getCollection()).isEqualTo("e1");
                    assertThat(e.getFrom()).containsExactlyInAnyOrder("a");
                    assertThat(e.getTo()).containsExactlyInAnyOrder("b", "c");
                })
                .anySatisfy(e -> {
                    assertThat(e.getCollection()).isEqualTo("e2");
                    assertThat(e.getFrom()).containsExactlyInAnyOrder("a", "b");
                    assertThat(e.getTo()).containsExactlyInAnyOrder("c", "d");
                });
        assertThat(conf.vertices).containsExactlyInAnyOrder("a", "b", "c", "d", "x", "y", "z");
        assertThat(conf.edges).containsExactlyInAnyOrder("e1", "e2");
        assertThat(conf.driverConfig.getHosts()).isPresent()
                .get(as(InstanceOfAssertFactories.list(HostDescription.class)))
                .hasSize(1)
                .anySatisfy(host -> {
                    assertThat(host.getHost()).isEqualTo("172.28.0.1");
                    assertThat(host.getPort()).isEqualTo(8529);
                });
        assertThat(conf.driverConfig.getPassword()).isPresent().get().isEqualTo("test");
    }

    private static org.apache.commons.configuration2.Configuration getConfiguration(final File configurationFile) {
        if (!configurationFile.isFile())
            throw new IllegalArgumentException(String.format("The location configuration must resolve to a file and [%s] does not", configurationFile));

        try {
            final String fileName = configurationFile.getName();
            final String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);

            final Configuration conf;
            final Configurations configs = new Configurations();

            switch (fileExtension) {
                case "yml":
                case "yaml":
                    final Parameters params = new Parameters();
                    final FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                            new FileBasedConfigurationBuilder<FileBasedConfiguration>(YAMLConfiguration.class).
                                    configure(params.fileBased().setFile(configurationFile));

                    final org.apache.commons.configuration2.Configuration copy = new org.apache.commons.configuration2.BaseConfiguration();
                    ConfigurationUtils.copy(builder.configure(params.fileBased().setFile(configurationFile)).getConfiguration(), copy);
                    conf = copy;
                    break;
                case "xml":
                    conf = configs.xml(configurationFile);
                    break;
                default:
                    conf = configs.properties(configurationFile);
            }
            return conf;
        } catch (ConfigurationException e) {
            throw new IllegalArgumentException(String.format("Could not load configuration at: %s", configurationFile), e);
        }
    }
}
