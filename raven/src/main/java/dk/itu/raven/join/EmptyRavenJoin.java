package dk.itu.raven.join;

import dk.itu.raven.join.results.IJoinResult;
import dk.itu.raven.join.results.JoinResult;

public class EmptyRavenJoin extends AbstractRavenJoin {

    @Override
    protected IJoinResult joinImplementation(IRasterFilterFunction function) {
        return new JoinResult();
    }

    @Override
    public long count() {
        return 0;
    }

}
