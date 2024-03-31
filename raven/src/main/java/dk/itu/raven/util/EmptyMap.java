package dk.itu.raven.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EmptyMap<K, V> implements Map<K, V> {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        return null;
    }

    @Override
    public V put(K key, V value) {
        return value;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
    }

    @Override
    public void clear() {
    }

    @Override
    public Set<K> keySet() {
        return new HashSet<>();
    }

    @Override
    public Collection<V> values() {
        return new HashSet<>();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new HashSet<>();
    }

}
