package dk.itu.raven.join;

import java.util.stream.Stream;

public class StreamedRavenJoin extends AbstractRavenJoin {
    private Stream<RavenJoin> ravenJoins;

    public StreamedRavenJoin(Stream<RavenJoin> ravenJoins) {

        this.ravenJoins = ravenJoins;
    }

    @Override
    protected StreamedJoinResult joinImplementation(IRasterFilterFunction function) {
        return new StreamedJoinResult(ravenJoins.map(rj -> {
            long start = System.currentTimeMillis();
            var res = rj.joinImplementation(function);
            long end = System.currentTimeMillis();
            System.out.println("joined in: " + (end - start) + "ms");
            return res;
        }));
    }

    public Stream<RavenJoin> getRavenJoins() {
        return this.ravenJoins;
    }
}
