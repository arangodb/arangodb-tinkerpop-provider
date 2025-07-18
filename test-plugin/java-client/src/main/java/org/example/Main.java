package org.example;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class Main {
    public static void main(String[] args) throws Exception {
        Cluster cluster = Cluster.build().addContactPoint("172.28.0.1").create();
        Client client = cluster.connect();
        try {
            GraphTraversalSource g = AnonymousTraversalSource.traversal().withRemote(DriverRemoteConnection.using(client, "g"));
            System.out.println(g.V().valueMap().toList());
            List<Map<String, Object>> res = g.V().as("a").out("knows").as("b").select("a", "b").by("name").toList();
            res.forEach(System.out::println);
            assertThat(res)
                    .anyMatch(x -> x.get("a").equals("marko") && x.get("b").equals("josh"))
                    .anyMatch(x -> x.get("a").equals("marko") && x.get("b").equals("vadas"));
            g.close();
        } finally {
            client.close();
            cluster.close();
        }
    }
}
