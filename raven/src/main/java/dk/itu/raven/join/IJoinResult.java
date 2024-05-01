package dk.itu.raven.join;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;

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

    public IJoinResult filter(Predicate<? super JoinResultItem> predicate);

    public Optional<JoinResultItem> find(Predicate<? super JoinResultItem> predicate);

}
