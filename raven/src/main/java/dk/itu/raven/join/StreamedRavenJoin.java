package dk.itu.raven.join;

import java.awt.Rectangle;
import java.util.stream.Stream;

import dk.itu.raven.geometry.Offset;

public class StreamedRavenJoin extends AbstractRavenJoin {
    private Stream<RavenJoin> ravenJoins;

    public StreamedRavenJoin(Stream<RavenJoin> ravenJoins, Rectangle rasterWindow) {
        super(rasterWindow);
        this.ravenJoins = ravenJoins;
    }

    @Override
    protected StreamedJoinResult joinImplementation(IRasterFilterFunction function) {
        return new StreamedJoinResult(ravenJoins.map(rj -> rj.joinImplementation(function)),
                new Offset<>(rasterWindow.x, rasterWindow.y));
    }

    public Stream<RavenJoin> getRavenJoins() {
        return this.ravenJoins;
    }
}
