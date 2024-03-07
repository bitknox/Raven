package dk.itu.raven.join;

import java.util.Iterator;

import dk.itu.raven.geometry.Offset;

public abstract class AbstractJoinResult implements Iterable<JoinResultItem> {
    // TODO: Figure out if this is still needed
    protected Offset<Integer> offset;

    public AbstractJoinResult() {
        this.offset = new Offset<Integer>(0, 0);
    }

    public AbstractJoinResult(Offset<Integer> offset) {
        this.offset = offset;
    }

    /**
     * Returns an iterator over the join result.
     * **WARNING**
     * This method converts a parallel join result to a sequential one.
     * **WARNING**
     */
    public abstract Iterator<JoinResultItem> iterator();

    public abstract int count();

    public abstract JoinResult asMemoryAllocatedResult();

    public Offset<Integer> getOffset() {
        return this.offset;
    }

}
