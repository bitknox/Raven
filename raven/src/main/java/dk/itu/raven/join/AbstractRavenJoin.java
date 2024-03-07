package dk.itu.raven.join;

public abstract class AbstractRavenJoin {

    protected abstract AbstractJoinResult joinImplementation(IRasterFilterFunction function);

    public AbstractJoinResult join(IRasterFilterFunction function) {
        AbstractJoinResult result = joinImplementation(function);
        return result;
    }

    public AbstractJoinResult join() {
        return join(JoinFilterFunctions.acceptAll());
    }
}
