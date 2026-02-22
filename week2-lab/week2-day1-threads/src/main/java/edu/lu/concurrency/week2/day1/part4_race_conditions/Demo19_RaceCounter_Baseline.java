package edu.lu.concurrency.week2.day1.part4_race_conditions;

public final class Demo19_RaceCounter_Baseline {

    private static final class Counter {
        static int count = 0;
    }

    public static void main(String[] args) throws InterruptedException {
        final int threads = Integer.getInteger("demo.threads", 10);
        final int iters   = Integer.getInteger("demo.iters", 100_000);
        final int runs    = Integer.getInteger("demo.runs", 5);

        final int expected = threads * iters;

        System.out.println("=== Demo19  Baseline Race (intentional bug) ===");
        System.out.println("[CONFIG] threads=" + threads + " iters=" + iters + " runs=" + runs);

        for (int r = 1; r <= runs; r++) {
            Counter.count = 0;

            Runnable task = () -> {
                for (int i = 0; i < iters; i++) {
                    Counter.count++; // NOT ATOMIC (race)
                }
            };

            Thread[] ts = new Thread[threads];
            for (int i = 0; i < threads; i++) {
                ts[i] = new Thread(task, "t" + i);
                ts[i].start();
            }
            for (Thread t : ts) t.join();

            System.out.println("[RUN] r=" + r + " expected=" + expected + " actual=" + Counter.count);
        }

        System.out.println("[TAKEAWAY] join() ensures completion, NOT atomicity.");
    }

    private Demo19_RaceCounter_Baseline() {}
}