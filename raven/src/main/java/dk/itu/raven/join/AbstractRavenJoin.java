package dk.itu.raven.join;

public abstract class AbstractRavenJoin {

    protected abstract IJoinResult joinImplementation(IRasterFilterFunction function);

    public IJoinResult join(IRasterFilterFunction function) {
        IJoinResult result = joinImplementation(function);
        return result;
    }

    public IJoinResult join() {
        return join(JoinFilterFunctions.acceptAll());
    }
}
