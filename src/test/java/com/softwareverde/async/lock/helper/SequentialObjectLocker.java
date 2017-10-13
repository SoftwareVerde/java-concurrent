package com.softwareverde.async.lock.helper;

import com.softwareverde.async.lock.MultiLock;

public class SequentialObjectLocker<T> extends Thread {
    private final MultiLock<T> _multiLock;
    private final T[] _objects;
    private Long _completionTime;

    public SequentialObjectLocker(final MultiLock<T> multiLock, final T[] objects) {
        _multiLock = multiLock;
        _objects = objects;
    }

    @Override
    public void run() {
        _multiLock.lock(_objects);
        _completionTime = System.currentTimeMillis();
        _multiLock.unlock(_objects);
    }

    public Long getTimeCompleted() {
        return _completionTime;
    }
}