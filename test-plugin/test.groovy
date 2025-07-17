:remote connect tinkerpop.server /arangodb/remote.yaml
:remote console

graph.io(IoCore.graphson()).readGraph("data/tinkerpop-modern.json")
try {
    assert g.V().as("a").out("knows").as("b").select("a", "b").by("name").toList().stream()
            .anyMatch(x -> x.get("a").equals("marko") && x.get("b").equals("vadas"))
} catch (AssertionError e) {
    // required to exit(1): gremlin console exits 1 only in case of Exception, not Error
    throw new RuntimeException(e)
}
println("DONE")
