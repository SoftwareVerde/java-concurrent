package com.softwareverde.concurrent.lock;

import java.util.concurrent.atomic.AtomicLong;

public class IndexLock {
    protected static long getThreadId() {
        final Thread thread = Thread.currentThread();
        return thread.getId();
    }

    protected final Object _pin = new Object();
    protected final AtomicLong[] _indexLocks;

    protected boolean _tryLock(final int index) {
        final long threadId = IndexLock.getThreadId();

        if (_indexLocks[index].compareAndSet(0L, threadId)) {
            return true;
        }

        return _indexLocks[index].compareAndSet(threadId, threadId); // Thread already owns the lock...
    }

    public IndexLock(final int indexCount) {
        _indexLocks = new AtomicLong[indexCount];
        for (int i = 0; i < indexCount; ++i) {
            _indexLocks[i] = new AtomicLong(0L);
        }
    }

    public void lock(final int index) {
        final long threadId = IndexLock.getThreadId();

        while (! _tryLock(index)) {
            synchronized (_pin) {
                try {
                    _pin.wait();
                }
                catch (final InterruptedException exception) {
                    throw new RuntimeException(exception);
                }
            }
        }
    }

    public void unlock(final int index) {
        final long threadId = IndexLock.getThreadId();

        _indexLocks[index].compareAndSet(threadId, 0L);

        synchronized (_pin) {
            _pin.notifyAll();
        }
    }
}
