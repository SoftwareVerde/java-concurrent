package com.softwareverde.async.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class BruteForceMultiLock<T> implements MultiLock<T> {
    private final ConcurrentHashMap<T, ReentrantLock> _lockedItems = new ConcurrentHashMap<T, ReentrantLock>();

    public void lock(final T... requiredObjects) {
        final List<ReentrantLock> acquiredLocks = new ArrayList<ReentrantLock>();

        Boolean allLocksWereAcquired = false;
        while (! allLocksWereAcquired) {
            allLocksWereAcquired = true;
            synchronized (_lockedItems) {
                for (final T object : requiredObjects) {
                    ReentrantLock lock = _lockedItems.get(object);
                    if (lock == null) {
                        lock = new ReentrantLock();
                        lock.lock();
                        _lockedItems.put(object, lock);
                        acquiredLocks.add(lock);
                    }
                    else {
                        Boolean lockWasAcquired = false;
                        try {
                            lockWasAcquired = lock.tryLock(10L, TimeUnit.MILLISECONDS);
                        }
                        catch (final InterruptedException e) { }

                        if (lockWasAcquired) {
                            acquiredLocks.add(lock);
                        }
                        else {
                            allLocksWereAcquired = false;
                            break;
                        }
                    }
                }

                if (! allLocksWereAcquired) {
                    for (final ReentrantLock lock : acquiredLocks) {
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                        }
                    }
                }
            }

            if (! allLocksWereAcquired) {
                try { Thread.sleep(10L); } catch (final Exception e) { }
            }
        }
    }

    public void unlock(final T... releasedObjects) {
        for (final T releasedObject : releasedObjects) {
            final java.util.concurrent.locks.ReentrantLock lock = _lockedItems.get(releasedObject);
            if (lock != null) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }
}
