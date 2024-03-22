package dk.itu.raven.util;

import java.util.Arrays;
import java.util.Iterator;

/**
 * A simple implementation of an ArrayList for integers. (Avoids copying the
 * entire array when resizing)
 */
public class GoodIntArrayList implements Iterable<Integer>, Iterator<Integer> {
    int size;
    int[] array;
    int idx;

    public GoodIntArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.array = new int[initialCapacity];
        } else if (initialCapacity == 0) {
            this.array = new int[2];
        } else {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
    }

    public GoodIntArrayList() {
        this(0);
    }

    public void sort() {
        Arrays.sort(array, 0, size);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int get(int index) {
        return array[index];
    }

    public void set(int index, int element) {
        size = index + 1 > size ? index + 1 : size;
        ensureExplicitCapacity(index + 1);
        array[index] = element;
    }

    public void add(int element) {
        this.set(size, element);
    }

    @Override
    public Iterator<Integer> iterator() {
        this.idx = 0;
        return this;
    }

    @Override
    public boolean hasNext() {
        return idx < size;
    }

    @Override
    public Integer next() {
        return this.array[idx++];
    }

    private void ensureExplicitCapacity(int minCapacity) {

        // overflow-conscious code
        if (minCapacity - array.length > 0)
            grow(minCapacity);
    }

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = array.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        // minCapacity is usually close to size, so this is a win:
        array = Arrays.copyOf(array, newCapacity);
    }
}
