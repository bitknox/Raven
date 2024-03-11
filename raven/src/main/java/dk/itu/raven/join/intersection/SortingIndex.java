package dk.itu.raven.join.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dk.itu.raven.ksquared.IntPointer;

public class SortingIndex implements IntersectionIndex {
    private Map<Integer, IntPointer> count;
    private List<Integer> intersections;

    public SortingIndex() {
        this.intersections = new ArrayList<>();
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
        Collections.sort(intersections);
        return this.intersections.iterator();
    }

}
