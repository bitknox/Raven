package dk.itu.raven.join.intersection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import dk.itu.raven.ksquared.IntPointer;
import dk.itu.raven.util.GoodIntArrayList;

public class IntArrayListIndex implements IntersectionIndex {
    private Map<Integer, IntPointer> count;
    private GoodIntArrayList intersections;

    public IntArrayListIndex() {
        this.intersections = new GoodIntArrayList();
        this.count = new HashMap<>();
    }

    @Override
    public void addIntersection(int x) {
        IntPointer ptr = count.get(x);
        if (ptr == null) {
            intersections.add(x);
            ptr = new IntPointer();
            count.put(x, ptr);
        }
        ptr.val++;
    }

    @Override
    public int getCount(int x) {
        return count.get(x).val;
    }

    @Override
    public Iterator<Integer> iterator() {
        intersections.sort();
        return this.intersections;
    }

}
