package dk.itu.raven.join;

import java.util.stream.Stream;

public class StreamedRavenJoin extends AbstractRavenJoin {
    private Stream<RavenJoin> ravenJoins;

    public StreamedRavenJoin(Stream<RavenJoin> ravenJoins) {
        this.ravenJoins = ravenJoins;
    }

    @Override
    public StreamedJoinResult join(IRasterFilterFunction function) {
        return new StreamedJoinResult(ravenJoins.map(rj -> rj.join(function)));
    }

    public Stream<RavenJoin> getRavenJoins() {
        return this.ravenJoins;
    }
}
