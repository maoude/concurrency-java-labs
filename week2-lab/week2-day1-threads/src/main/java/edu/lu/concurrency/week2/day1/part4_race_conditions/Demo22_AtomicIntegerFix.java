package edu.lu.concurrency.week2.day1.part4_race_conditions;

import java.util.concurrent.atomic.AtomicInteger;

public final class Demo22_AtomicIntegerFix {

    public static void main(String[] args) throws InterruptedException {
        final int threads = Integer.getInteger("demo.threads", 10);
        final int iters   = Integer.getInteger("demo.iters", 100_000);
        final int expected = threads * iters;

        System.out.println("=== Demo22  AtomicInteger Fix ===");

        AtomicInteger counter = new AtomicInteger(0);

        Runnable task = () -> {
            for (int i = 0; i < iters; i++) {
                counter.incrementAndGet();
            }
        };

        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(task, "t" + i);
            ts[i].start();
        }
        for (Thread t : ts) t.join();

        System.out.println("[RESULT] expected=" + expected + " actual=" + counter.get());
        System.out.println("[TAKEAWAY] AtomicInteger provides atomic increments without synchronized.");
    }

    private Demo22_AtomicIntegerFix() {}
}