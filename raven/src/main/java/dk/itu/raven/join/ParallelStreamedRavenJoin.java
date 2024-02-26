package dk.itu.raven.join;

import java.awt.Rectangle;
import java.util.stream.Stream;

public class ParallelStreamedRavenJoin extends StreamedRavenJoin {

    public ParallelStreamedRavenJoin(Stream<RavenJoin> stream, Rectangle rasterWindow) {
        super(stream.parallel(), rasterWindow);
    }
}
