package dk.itu.raven.join.results;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class JoinResult implements IJoinResult {

    private List<JoinResultItem> list;

    public JoinResult() {
        this.list = new ArrayList<>();
    }

    JoinResult(List<JoinResultItem> list) {

        this.list = list;
    }

    public void add(JoinResultItem item) {
        this.list.add(item);
    }

    public JoinResultItem get(int index) {
        return this.list.get(index);
    }

    public int size() {
        return this.list.size();
    }

    @Override
    public Iterator<JoinResultItem> iterator() {
        return new Iterator<JoinResultItem>() {
            private final Iterator<JoinResultItem> iter = list.iterator();

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public JoinResultItem next() {
                return iter.next();
            }
        };
    }

    public List<JoinResultItem> getList() {
        return this.list;
    }

    @Override
    public long count() {
        long val = 0;
        for (var res : list) {
            for (var range : res.pixelRanges) {
                val += ((PixelRangeValue) range).x2 - ((PixelRangeValue) range).x1 + 1;
            }
        }
        return val;
    }

    @Override
    public JoinResult asMemoryAllocatedResult() {
        return this;
    }

    @Override
    public JoinResult filter(Predicate<? super JoinResultItem> predicate) {
        return new JoinResult(this.list.stream().filter(predicate).toList());
    }

    @Override
    public Optional<JoinResultItem> find(Predicate<? super JoinResultItem> predicate) {
        int index = list.size() - 1;
        while (index >= 0 && !predicate.test(list.get(index)))
            ;
        if (index < 0) {
            return Optional.empty();
        }
        return Optional.of(list.get(index));
    }

    public void forAll(Consumer<JoinResultItem> function) {
        for (JoinResultItem item : list) {
            function.accept(item);
        }
    }
}
