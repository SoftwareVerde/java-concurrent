package com.softwareverde.async;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashSet<T> implements Set<T> {
    protected final Set<T> _set = Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>());

    @Override
    public int size() {
        return _set.size();
    }

    @Override
    public boolean isEmpty() {
        return _set.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return _set.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return _set.iterator();
    }

    @Override
    public Object[] toArray() {
        return _set.toArray();
    }

    @Override
    public <T1> T1[] toArray(final T1[] a) {
        return _set.toArray(a);
    }

    @Override
    public boolean add(final T t) {
        return _set.add(t);
    }

    @Override
    public boolean remove(final Object o) {
        return _set.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return _set.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends T> c) {
        return _set.addAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return _set.retainAll(c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return _set.removeAll(c);
    }

    @Override
    public void clear() {
        _set.clear();
    }
}
