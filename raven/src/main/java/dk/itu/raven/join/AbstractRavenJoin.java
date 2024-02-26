package dk.itu.raven.join;

import java.awt.Rectangle;

public abstract class AbstractRavenJoin {
    protected Rectangle rasterWindow;

    public AbstractRavenJoin(Rectangle rasterWindow) {
        this.rasterWindow = rasterWindow;
    }

    protected abstract AbstractJoinResult joinImplementation(IRasterFilterFunction function);

    public AbstractJoinResult join(IRasterFilterFunction function) {
        AbstractJoinResult result = joinImplementation(function);
        return result;
    }

    public AbstractJoinResult join() {
        return join(JoinFilterFunctions.acceptAll());
    }
}
