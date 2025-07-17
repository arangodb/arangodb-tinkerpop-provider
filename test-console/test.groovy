graph = GraphFactory.open("/arangodb/arangodb.yaml")
graph.io(IoCore.graphson()).readGraph("data/tinkerpop-modern.json")
g = graph.traversal()
res = g.V().as("a").out("knows").as("b").select("a", "b").by("name").toList()
try {
    assert res.stream().anyMatch(x ->
            x.get("a").equals("marko") && x.get("b").equals("vadas"))
    assert res.stream().anyMatch(x ->
            x.get("a").equals("marko") && x.get("b").equals("josh"))
} catch (AssertionError e) {
    // required to exit(1): gremlin console exits 1 only in case of Exception, not Error
    throw new RuntimeException(e)
} finally {
    graph.close()
}
println("DONE")
