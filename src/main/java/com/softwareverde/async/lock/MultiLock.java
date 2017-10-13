package com.softwareverde.async.lock;

public interface MultiLock<T> {
    void lock(final T... requiredObjects);
    void unlock(final T... releasedObjects);
}
