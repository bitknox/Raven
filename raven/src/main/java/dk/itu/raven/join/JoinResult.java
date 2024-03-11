package dk.itu.raven.join;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    public int count() {
        return size();
    }

    @Override
    public JoinResult asMemoryAllocatedResult() {
        return this;
    }

}
