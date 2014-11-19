package com.sendish.api.util;

import java.util.ConcurrentModificationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class EntitySynchronizer {

    private static final int                                         DEFAULT_MAXIMUM_LOCK_DURATION_SEC = 10;
    private final transient ConcurrentHashMap<Object, ReentrantLock> locks                             = new ConcurrentHashMap<Object, ReentrantLock>();
    private static ThreadLocal<Object>                               keyThreadLocal                    = new ThreadLocal<Object>();
    private final transient int                                      maximumLockDurationSec;

    public EntitySynchronizer() {
        this(DEFAULT_MAXIMUM_LOCK_DURATION_SEC);
    }

    public EntitySynchronizer(final int p_maximumLockDurationSeconds) {
        maximumLockDurationSec = p_maximumLockDurationSeconds;
    }

    /**
     * Initiate a lock for all threads with this key value
     * @param p_key the instance identifier for concurrency synchronization
     */
    public final void lock(final Object p_key) {
        if (p_key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        /*
         * returns the existing lock for specified key, or null if there was no existing lock for the
         * key
         */
        ReentrantLock lock = new ReentrantLock(true);
        final ReentrantLock oldLock = locks.putIfAbsent(p_key, lock);
        lock = (oldLock == null ? lock : oldLock);
        /*
         * Acquires the lock and returns immediately with the value true if it is not held by another
         * thread within the given waiting time and the current thread has not been interrupted. If this
         * lock has been set to use a fair ordering policy then an available lock will NOT be acquired
         * if any other threads are waiting for the lock. If the current thread already holds this lock
         * then the hold count is incremented by one and the method returns true. If the lock is held by
         * another thread then the current thread becomes disabled for thread scheduling purposes and
         * lies dormant until one of three things happens: - The lock is acquired by the current thread;
         * or - Some other thread interrupts the current thread; or - The specified waiting time elapses
         */
        try {
            /*
             * using tryLock(timeout) instead of lock() to prevent deadlock situation in case acquired
             * lock is not released normalRelease will be false if the lock was released because the
             * timeout expired
             */
            final boolean normalRelease = lock.tryLock(maximumLockDurationSec, TimeUnit.SECONDS);
            /*
             * lock was release because timeout expired. We do not want to proceed, we should throw a
             * concurrency exception for waiting thread
             */
            if (!normalRelease) {
                throw new ConcurrentModificationException("Entity synchronization concurrency lock timeout expired for item key: " + p_key);
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException("Entity synchronization interrupted exception for item key: " + p_key);
        }
        keyThreadLocal.set(p_key);
    }

    /**
     * Unlock this thread's lock. This takes care of preserving the lock for any waiting threads with
     * the same entity key
     */
    public final void unlock() {
        final Object key = keyThreadLocal.get();
        keyThreadLocal.remove();
        if (key != null) {
            final ReentrantLock lock = locks.get(key);
            if (lock != null) {
                try {
                    if (!lock.hasQueuedThreads()) {
                        locks.remove(key);
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }

}
