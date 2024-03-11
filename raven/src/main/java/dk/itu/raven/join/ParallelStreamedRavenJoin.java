package dk.itu.raven.join;

import java.util.stream.Stream;

public class ParallelStreamedRavenJoin extends StreamedRavenJoin {

    public ParallelStreamedRavenJoin(Stream<RavenJoin> stream) {
        super(stream.parallel());
    }
}
