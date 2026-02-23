package edu.lu.concurrency.week1.lab1;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SQLiteConcurrencyDemo {

    private static final ReentrantReadWriteLock lock =
            new ReentrantReadWriteLock();

    private static int data = 0;

    public static void main(String[] args) throws Exception {

        int readers = 10;
        int writers = 2;

        // Readers
        for (int i = 0; i < readers; i++) {
            new Thread(() -> {
                while (true) {
                    lock.readLock().lock();
                    try {
                        int x = data;
                    } finally {
                        lock.readLock().unlock();
                    }
                }
            }).start();
        }

        // Writers
        for (int i = 0; i < writers; i++) {
            new Thread(() -> {
                while (true) {
                    lock.writeLock().lock();
                    try {
                        data++;
                        Thread.sleep(50); // simulate write
                    } catch (InterruptedException ignored) {
                    } finally {
                        lock.writeLock().unlock();
                    }
                }
            }).start();
        }
    }
}

