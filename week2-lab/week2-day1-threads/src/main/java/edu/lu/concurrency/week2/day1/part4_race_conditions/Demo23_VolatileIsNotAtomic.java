package edu.lu.concurrency.week2.day1.part4_race_conditions;

public final class Demo23_VolatileIsNotAtomic {

    private static final class Counter {
        static volatile int count = 0; // visibility yes, atomicity NO
    }

    public static void main(String[] args) throws InterruptedException {
        final int threads = Integer.getInteger("demo.threads", 10);
        final int iters   = Integer.getInteger("demo.iters", 100_000);
        final int expected = threads * iters;

        System.out.println("=== Demo23  volatile != atomic ===");

        Counter.count = 0;

        Runnable task = () -> {
            for (int i = 0; i < iters; i++) {
                Counter.count++; // still not atomic
            }
        };

        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(task, "t" + i);
            ts[i].start();
        }
        for (Thread t : ts) t.join();

        System.out.println("[RESULT] expected=" + expected + " actual=" + Counter.count);
        System.out.println("[TAKEAWAY] volatile fixes visibility, NOT lost updates.");
    }

    private Demo23_VolatileIsNotAtomic() {}
}