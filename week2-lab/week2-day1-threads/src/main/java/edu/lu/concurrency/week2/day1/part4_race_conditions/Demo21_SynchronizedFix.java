package edu.lu.concurrency.week2.day1.part4_race_conditions;

public final class Demo21_SynchronizedFix {

    private static final Object LOCK = new Object();

    private static final class Counter {
        static int count = 0;
    }

    public static void main(String[] args) throws InterruptedException {
        final int threads = Integer.getInteger("demo.threads", 10);
        final int iters   = Integer.getInteger("demo.iters", 100_000);
        final int expected = threads * iters;

        System.out.println("=== Demo21  synchronized Fix ===");

        Counter.count = 0;

        Runnable task = () -> {
            for (int i = 0; i < iters; i++) {
                synchronized (LOCK) {
                    Counter.count++;
                }
            }
        };

        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(task, "t" + i);
            ts[i].start();
        }
        for (Thread t : ts) t.join();

        System.out.println("[RESULT] expected=" + expected + " actual=" + Counter.count);
        System.out.println("[TAKEAWAY] mutual exclusion removes lost updates.");
    }

    private Demo21_SynchronizedFix() {}
}