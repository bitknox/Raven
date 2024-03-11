package dk.itu.raven.join;

import java.util.Iterator;

public interface IJoinResult extends Iterable<JoinResultItem> {

    /**
     * Returns an iterator over the join result.
     * **WARNING**
     * This method converts a parallel join result to a sequential one.
     * **WARNING**
     */
    public abstract Iterator<JoinResultItem> iterator();

    public abstract int count();

    public abstract JoinResult asMemoryAllocatedResult();

}
