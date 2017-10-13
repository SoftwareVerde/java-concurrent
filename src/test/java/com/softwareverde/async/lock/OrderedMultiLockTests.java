package com.softwareverde.async.lock;

import com.softwareverde.async.lock.helper.SequentialObjectLocker;
import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;

public class OrderedMultiLockTests {
    @Test
    public void should_only_succeed_once_all_locks_are_obtained() throws Exception {
        final Integer runCount = 100;
        for (int i=0; i<runCount; ++i) {
            // Setup
            final Integer[] contestedObjects = new Integer[10];
            for (int j = 0; j < contestedObjects.length; ++j) {
                contestedObjects[j] = j;
            }

            final MultiLock<Integer> multiLock = new OrderedMultiLock<Integer>(new Comparator<Integer>() {
                @Override
                public int compare(final Integer o1, final Integer o2) {
                    return o1.compareTo(o2);
                }
            });
            final SequentialObjectLocker<Integer> objectLockerThread = new SequentialObjectLocker<Integer>(multiLock, contestedObjects);
            Long timeWhenAllUnlocked = Long.MAX_VALUE;

            // Action
            multiLock.lock(contestedObjects);
            objectLockerThread.start();
            try { Thread.sleep(10L); } catch (final Exception e) { }
            for (int j = contestedObjects.length - 1; j >= 0; --j) {
                timeWhenAllUnlocked = System.currentTimeMillis();
                multiLock.unlock(contestedObjects[j]);
                // System.out.println("Unlocked " + (j + 1) + " of " + contestedObjects.length);
            }
            objectLockerThread.join();
            final Long timeWhenThreadCompleted = objectLockerThread.getTimeCompleted();

            // Assert
            Assert.assertTrue(timeWhenThreadCompleted >= timeWhenAllUnlocked);
            System.out.println("Completed " + (i + 1) + " of " + runCount);
        }
    }
}
