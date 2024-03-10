package dk.itu.raven.join.intersection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import dk.itu.raven.ksquared.IntPointer;
import dk.itu.raven.util.BST;

public class FastBSTIntersectionIndex implements IntersectionIndex {
    private Map<Integer, IntPointer> count;
    private BST<Integer, Integer> bst; // in this class, we only a BST set, not a map

    public FastBSTIntersectionIndex(int size) {
        this.bst = new BST<>();
        this.count = new HashMap<>();
    }

    @Override
    public void addIntersection(int x) {
        IntPointer ptr = count.get(x);
        if (ptr == null) {
            bst.put(x, 0);
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
        return bst.keys().iterator();
    }
}
