package com.softwareverde.async.lock.helper;

import com.softwareverde.async.lock.MultiLock;

import java.util.HashSet;
import java.util.Set;

public class RandomObjectLocker extends Thread {
    private final MultiLock<Object> _multiLock;
    private final Object[] _objects;
    private final Long _sleepTime;

    public RandomObjectLocker(final MultiLock<Object> multiLock, final Integer objectCount, final Object[] objects, final Long sleepTime) {
        _multiLock = multiLock;
        _sleepTime = sleepTime;

        final Set<Integer> lockedObjectIds = new HashSet<Integer>();
        _objects = new Object[objectCount];

        for (int i=0; i<objectCount; ++i) {
            Integer randomNumber;
            do {
                randomNumber = _getRandomIndex();
            } while (lockedObjectIds.contains(randomNumber));

            lockedObjectIds.add(randomNumber);
            final Object object = objects[randomNumber];
            _objects[i] = object;
        }

        _multiLock.lock(_objects);
        System.out.println(objectCount + " locks acquired.");
    }

    private Integer _getRandomIndex() {
        return ((int) (Math.random() * 7777777)) % _objects.length;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(_sleepTime);
        }
        catch (final InterruptedException exception) { }

        _multiLock.unlock(_objects);
    }
}