package com.softwareverde.async.lock;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class OrderedMultiLock<T> implements MultiLock<T> {
    private final ConcurrentHashMap<T, ReentrantLock> _lockedItems = new ConcurrentHashMap<T, ReentrantLock>();
    private final Comparator<T> _comparator;
    private final Comparator<T> _reverseComparator;

    public OrderedMultiLock(final Comparator<T> comparator) {
        _comparator = comparator;
        _reverseComparator = Collections.reverseOrder(comparator);
    }

    public void lock(final T... requiredObjects) {
        final List<T> sortedRequiredObjects = Arrays.asList(requiredObjects);
        Collections.sort(sortedRequiredObjects, _comparator);

        for (final T requiredObject : sortedRequiredObjects) {
            synchronized (_lockedItems) {
                if (!_lockedItems.containsKey(requiredObject)) {
                    _lockedItems.put(requiredObject, new ReentrantLock());
                }
            }
        }

        for (final T requiredObject : sortedRequiredObjects) {
            final ReentrantLock lock = _lockedItems.get(requiredObject);
            lock.lock();
        }
    }

    public void unlock(final T... releasedObjects) {
        final List<T> sortedReleasedObjects = Arrays.asList(releasedObjects);
        Collections.sort(sortedReleasedObjects, _reverseComparator);

        synchronized (_lockedItems) {
            for (final T releasedObject : sortedReleasedObjects) {
                final ReentrantLock lock = _lockedItems.get(releasedObject);
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }
}
