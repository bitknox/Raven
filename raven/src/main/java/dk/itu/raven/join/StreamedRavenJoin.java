package dk.itu.raven.join;

import java.util.stream.Stream;

import dk.itu.raven.join.results.StreamedJoinResult;

public class StreamedRavenJoin extends AbstractRavenJoin {
    private Stream<RavenJoin> ravenJoins;

    public StreamedRavenJoin(Stream<RavenJoin> ravenJoins) {

        this.ravenJoins = ravenJoins;
    }

    @Override
    protected StreamedJoinResult joinImplementation(IRasterFilterFunction function) {
        return new StreamedJoinResult(ravenJoins.map(rj -> rj.joinImplementation(function)));
    }

    public Stream<RavenJoin> getRavenJoins() {
        return this.ravenJoins;
    }
}
