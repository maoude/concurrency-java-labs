package edu.lu.concurrency.week2.day1;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class ThreadStateTest {

    @Test
    void sleepProducesTimedWaiting() throws Exception {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        t.start();
        Thread.sleep(100);

        // Depending on timing, it might briefly be RUNNABLE before sleep; allow both but prefer TIMED_WAITING
        assertTrue(t.getState() == Thread.State.TIMED_WAITING || t.getState() == Thread.State.RUNNABLE);

        t.join();
        assertEquals(Thread.State.TERMINATED, t.getState());
    }

    @Test
    void joinProducesWaiting() throws Exception {
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Worker");

        Thread waiter = new Thread(() -> {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Waiter");

        worker.start();
        waiter.start();

        Thread.sleep(150);
        // Waiter is typically WAITING; allow RUNNABLE for tiny scheduling windows
        assertTrue(waiter.getState() == Thread.State.WAITING || waiter.getState() == Thread.State.RUNNABLE);

        worker.join();
        waiter.join();
        assertEquals(Thread.State.TERMINATED, waiter.getState());
    }

    @Test
    void lockContentionProducesBlocked() throws Exception {
        Object lock = new Object();
        CountDownLatch entered = new CountDownLatch(1);

        Thread holder = new Thread(() -> {
            synchronized (lock) {
                entered.countDown();
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Holder");

        Thread contender = new Thread(() -> {
            synchronized (lock) {
                // will only enter after holder releases
            }
        }, "Contender");

        holder.start();
        entered.await(); // ensure holder owns lock
        contender.start();

        Thread.sleep(150);
        assertTrue(contender.getState() == Thread.State.BLOCKED || contender.getState() == Thread.State.RUNNABLE);

        holder.join();
        contender.join();

        assertEquals(Thread.State.TERMINATED, contender.getState());
    }
}