package dk.itu.raven.join;

import dk.itu.raven.join.results.IJoinResult;

public abstract class AbstractRavenJoin {

    protected abstract IJoinResult joinImplementation(IRasterFilterFunction function);

    public IJoinResult join(IRasterFilterFunction function) {
        IJoinResult result = joinImplementation(function);
        return result;
    }

    public IJoinResult join() {
        return join(JoinFilterFunctions.acceptAll());
    }

    public abstract long count();
}
