package dk.itu.raven.join.intersection;

import java.util.Iterator;

import dk.itu.raven.ksquared.IntPointer;
import dk.itu.raven.util.BST;

public class BSTIndex2 implements IntersectionIndex {

    private BST<Integer, IntPointer> bst;

    public BSTIndex2() {
        this.bst = new BST<>();
    }

    @Override
    public Iterator<Integer> iterator() {
        return this.bst.keys().iterator();
    }

    @Override
    public void addIntersection(int x) {
        IntPointer num = bst.get(x);
        if (num == null) {
            num = new IntPointer();
            num.val++;
            bst.put(x, num);
        }
        num.val++;
    }

    @Override
    public int getCount(int x) {
        return bst.get(x).val;
    }

}
