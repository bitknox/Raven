package dk.itu.raven.util;

import java.util.Arrays;

/**
 * A simple implementation of an ArrayList for integers. (Avoids copying the
 * entire array when resizing)
 */
public class GoodLongArrayList {
    int size;
    long[] array;

    public GoodLongArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.array = new long[initialCapacity];
        } else if (initialCapacity == 0) {
            this.array = new long[2];
        } else {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
    }

    public GoodLongArrayList() {
        this(0);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public long get(int index) {
        return array[index];
    }

    public void set(int index, long element) {
        size = index + 1 > size ? index + 1 : size;
        ensureExplicitCapacity(index + 1);
        array[index] = element;
    }

    public void add(long element) {
        set(this.size, element);
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
