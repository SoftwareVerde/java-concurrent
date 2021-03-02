package com.softwareverde.concurrent.lock;

import com.softwareverde.logging.LogLevel;
import com.softwareverde.logging.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class IndexLockTests {
    protected static class Thread extends java.lang.Thread {
        final AtomicBoolean startPin = new AtomicBoolean(false);
        final AtomicBoolean lockAcquiredPin;

        public Thread(final AtomicBoolean lockAcquiredPin, final Runnable runnable) {
            super(runnable);
            this.lockAcquiredPin = lockAcquiredPin;
        }

        @Override
        public void run() {
            synchronized (startPin) {
                startPin.set(true);
                startPin.notifyAll();
            }

            super.run();
        }
    }

    protected static Thread lockAndUnlock(final int index, final IndexLock indexLock, final long waitTime, final AtomicLong timeExecuted) throws Exception {
        final AtomicBoolean lockAcquiredPin = new AtomicBoolean(false);

        final Thread thread = new Thread(lockAcquiredPin, new Runnable() {
            @Override
            public void run() {
                try {
                    final long start;
                    synchronized (lockAcquiredPin) {
                        start = System.currentTimeMillis();
                        indexLock.lock(index);
                        lockAcquiredPin.set(true);
                        lockAcquiredPin.notifyAll();
                    }
                    Thread.sleep(waitTime);
                    indexLock.unlock(index);
                    final long end = System.currentTimeMillis();

                    synchronized (timeExecuted) {
                        final long executionTime = (end - start);
                        System.out.println(Thread.currentThread().getId() + " executed for " + executionTime + "ms.");
                        timeExecuted.set(executionTime);
                        timeExecuted.notifyAll();
                    }
                }
                catch (final Exception exception) { }
            }
        });

        return thread;
    }

    protected static void startThread(final Thread thread) throws InterruptedException {
        System.out.println("Starting thread " + thread.getId() + ".");
        synchronized (thread.startPin) {
            thread.startPin.set(false);
            thread.start();
            if (! thread.startPin.get()) {
                thread.startPin.wait();
            }
        }
    }

    protected static void waitForLockAcquired(final Thread thread) throws InterruptedException {
        synchronized (thread.lockAcquiredPin) {
            if (! thread.lockAcquiredPin.get()) {
                thread.lockAcquiredPin.wait();
            }
        }
    }

    @BeforeClass
    public static void setUp() {
        Logger.setLogLevel(LogLevel.ON);
    }

    @Test
    public void should_wait_until_lock_is_acquired() throws Exception {
        // Setup
        final IndexLock indexLock = new IndexLock(32);
        final AtomicLong initialLockThreadExecutionTime = new AtomicLong();
        final AtomicLong contentiousThreadExecutionTime = new AtomicLong();

        // Action
        final Thread initialLockThread = IndexLockTests.lockAndUnlock(0, indexLock, 1000L, initialLockThreadExecutionTime);

        {
            final Thread contentiousThread = IndexLockTests.lockAndUnlock(0, indexLock, 0L, contentiousThreadExecutionTime);

            IndexLockTests.startThread(initialLockThread);
            waitForLockAcquired(initialLockThread); // Guarantee that the initialLockThread does not get beat by the contentiousLockThread...

            IndexLockTests.startThread(contentiousThread);
            contentiousThread.join();
            initialLockThread.join();
        }

        // Assert
        Assert.assertTrue(contentiousThreadExecutionTime.get() >= 1000);
        Assert.assertTrue(contentiousThreadExecutionTime.get() <= 1200L);
    }

    @Test
    public void should_not_block_other_indexes() throws Exception {
        // Setup
        final IndexLock indexLock = new IndexLock(32);
        final AtomicLong initialLockThreadExecutionTime = new AtomicLong();
        final AtomicLong nonContentiousThreadExecutionTime = new AtomicLong();

        // Action
        final Thread initialLockThread = IndexLockTests.lockAndUnlock(0, indexLock, 1000L, initialLockThreadExecutionTime);

        {
            final Thread nonContentiousThread = IndexLockTests.lockAndUnlock(1, indexLock, 1000L, nonContentiousThreadExecutionTime);

            IndexLockTests.startThread(initialLockThread);
            waitForLockAcquired(initialLockThread);
            IndexLockTests.startThread(nonContentiousThread);
            nonContentiousThread.join();
            initialLockThread.join();
        }

        // Assert
        Assert.assertTrue(initialLockThreadExecutionTime.get() >= 1000);
        Assert.assertTrue(initialLockThreadExecutionTime.get() <= 1200);

        Assert.assertTrue(nonContentiousThreadExecutionTime.get() >= 1000);
        Assert.assertTrue(nonContentiousThreadExecutionTime.get() <= 1200);
    }

    @Test
    public void all_threads_should_wait_until_lock_is_acquired() throws Exception {
        // Setup
        final IndexLock indexLock = new IndexLock(32);
        final AtomicLong initialLockThreadExecutionTime = new AtomicLong();
        final AtomicLong contentiousThread0ExecutionTime = new AtomicLong();
        final AtomicLong contentiousThread1ExecutionTime = new AtomicLong();
        final AtomicLong nonContentiousThreadExecutionTime = new AtomicLong();

        // Action
        final Thread initialLockThread = IndexLockTests.lockAndUnlock(0, indexLock, 1000L, initialLockThreadExecutionTime);

        {
            final Thread contentiousThread0 = IndexLockTests.lockAndUnlock(0, indexLock, 500L, contentiousThread0ExecutionTime);
            final Thread contentiousThread1 = IndexLockTests.lockAndUnlock(0, indexLock, 500L, contentiousThread1ExecutionTime);
            final Thread nonContentiousThread = IndexLockTests.lockAndUnlock(1, indexLock, 1000L, nonContentiousThreadExecutionTime);

            IndexLockTests.startThread(initialLockThread);
            waitForLockAcquired(initialLockThread);

            IndexLockTests.startThread(nonContentiousThread);
            IndexLockTests.startThread(contentiousThread0);
            IndexLockTests.startThread(contentiousThread1);
            contentiousThread1.join();
            contentiousThread0.join();
            nonContentiousThread.join();
            initialLockThread.join();
        }

        // Assert
        Assert.assertTrue(initialLockThreadExecutionTime.get() >= 1000);
        Assert.assertTrue(initialLockThreadExecutionTime.get() <= 1200);

        Assert.assertTrue(nonContentiousThreadExecutionTime.get() >= 1000);
        Assert.assertTrue(nonContentiousThreadExecutionTime.get() <= 1200);

        Assert.assertTrue(contentiousThread0ExecutionTime.get() >= 1000);
        Assert.assertTrue(contentiousThread1ExecutionTime.get() >= 1000);

        final long firstThreadMaxRunTime = 1000L;
        final long secondThreadRunTime = (firstThreadMaxRunTime + 500L);
        final long thirdThreadRunTime = (secondThreadRunTime + 500L);
        final long bufferRunTime = 200L;
        final long totalMaxRunTime = (firstThreadMaxRunTime + secondThreadRunTime + thirdThreadRunTime + bufferRunTime);
        System.out.println(initialLockThreadExecutionTime.get() + " + " + contentiousThread0ExecutionTime.get() + " + " + contentiousThread1ExecutionTime.get() + " <= " + totalMaxRunTime);
        Assert.assertTrue((initialLockThreadExecutionTime.get() + contentiousThread0ExecutionTime.get() + contentiousThread1ExecutionTime.get()) <= totalMaxRunTime);
    }

}
