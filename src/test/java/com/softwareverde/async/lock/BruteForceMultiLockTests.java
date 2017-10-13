package com.softwareverde.async.lock;

import com.softwareverde.async.lock.helper.SequentialObjectLocker;
import org.junit.Assert;
import org.junit.Test;

public class BruteForceMultiLockTests {
    @Test
    public void should_only_succeed_once_all_locks_are_obtained() throws Exception {
        final Integer runCount = 100;
        for (int i=0; i<runCount; ++i) {
            // Setup
            final Object[] contestedObjects = new Object[10];
            for (int j = 0; j < contestedObjects.length; ++j) {
                contestedObjects[j] = new Object();
            }

            final MultiLock<Object> multiLock = new BruteForceMultiLock<Object>();
            final SequentialObjectLocker<Object> objectLocker = new SequentialObjectLocker<Object>(multiLock, contestedObjects);
            Long timeWhenAllUnlocked = Long.MAX_VALUE;

            // Action
            multiLock.lock(contestedObjects);
            objectLocker.start();
            try { Thread.sleep(10L); } catch (final Exception e) { }
            for (int j = 0; j < contestedObjects.length; ++j) {
                timeWhenAllUnlocked = System.currentTimeMillis();
                multiLock.unlock(contestedObjects[j]);
                // System.out.println("Unlocked " + (j + 1) + " of " + contestedObjects.length);
            }
            objectLocker.join();
            final Long timeWhenThreadCompleted = objectLocker.getTimeCompleted();

            // Assert
            Assert.assertTrue(timeWhenThreadCompleted >= timeWhenAllUnlocked);
            System.out.println("Completed " + (i + 1) + " of " + runCount);
        }
    }
}
