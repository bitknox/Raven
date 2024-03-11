package dk.itu.raven.join.intersection;

import java.util.Iterator;

import dk.itu.raven.util.BST;

public class BSTIndex implements IntersectionIndex {

    private BST<Integer, Integer> bst;

    public BSTIndex() {
        this.bst = new BST<>();
    }

    @Override
    public Iterator<Integer> iterator() {
        return this.bst.keys().iterator();
    }

    @Override
    public void addIntersection(int x) {
        Integer num = bst.get(x);
        if (num == null) {
            bst.put(x, 1);
        } else {
            bst.put(x, num + 1);
        }
    }

    @Override
    public int getCount(int x) {
        return bst.get(x);
    }

}
