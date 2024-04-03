package dk.itu.raven.join;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ArrayMap<T> implements Map<Integer, T> {
    T[] array;
    // BitMap contained;
    int size;
    int offset;

    public ArrayMap(int size, int offset) {
        this.array = (T[]) new Object[size];
        // this.contained = new BitMap(size);
        this.size = size;
        this.offset = offset;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'containsValue'");
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'containsValue'");
    }

    @Override
    public boolean containsKey(Object key) {
        int idx = (Integer) key;
        idx -= offset;
        return idx < size && array[idx] != null;
    }

    @Override
    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'containsValue'");
    }

    @Override
    public T get(Object key) {
        int idx = (Integer) key;
        idx -= offset;
        return array[idx];
    }

    @Override
    public T put(Integer idx, T value) {
        idx -= offset;
        T old = array[idx];
        array[idx] = value;
        return old;
    }

    @Override
    public T remove(Object key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends T> m) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'putAll'");
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clear'");
    }

    @Override
    public Set<Integer> keySet() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'keySet'");
    }

    @Override
    public Collection<T> values() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'values'");
    }

    @Override
    public Set<Entry<Integer, T>> entrySet() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'entrySet'");
    }

}
