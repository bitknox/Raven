
package dk.itu.raven.join;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Logger.LogLevel;

public class StreamedJoinResult implements IJoinResult {
    private Stream<JoinResult> stream;

    public StreamedJoinResult(Stream<JoinResult> stream) {
        super();
        this.stream = stream;
    }

    @Override
    public Iterator<JoinResultItem> iterator() {
        if (stream.isParallel()) {
            Logger.log("WARNING: Using parallel stream sequentially. Consider using forEach instead", LogLevel.WARNING);
        }
        return new Iterator<JoinResultItem>() {
            private final Iterator<JoinResult> iter = stream.iterator();
            private Iterator<JoinResultItem> current = Collections.emptyIterator();

            @Override
            public boolean hasNext() {
                if (current.hasNext())
                    return true;
                if (!iter.hasNext())
                    return false;
                current = iter.next().iterator();
                return hasNext();
            }

            @Override
            public JoinResultItem next() {
                return current.next();
            }
        };
    }

    public Stream<JoinResult> getStream() {
        return this.stream;
    }

    @Override
    public void forEach(Consumer<? super JoinResultItem> action) {
        stream.forEach(lst -> {
            lst.forEach(action);
        });
    }

    @Override
    public int count() {
        return stream.collect(Collectors.summingInt(JoinResult::count));
    }

    @Override
    public JoinResult asMemoryAllocatedResult() {
        ArrayList<JoinResultItem> items = this.stream.collect(ArrayList::new, (l, x) -> l.addAll(x.getList()),
                (l, r) -> l.addAll(r));
        JoinResult result = new JoinResult(items);

        return result;
    }

}
