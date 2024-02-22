package dk.itu.raven.join;

public abstract class AbstractRavenJoin {
    public abstract AbstractJoinResult join(IRasterFilterFunction function);

    public AbstractJoinResult join() {
        return join(JoinFilterFunctions.acceptAll());
    }
}
