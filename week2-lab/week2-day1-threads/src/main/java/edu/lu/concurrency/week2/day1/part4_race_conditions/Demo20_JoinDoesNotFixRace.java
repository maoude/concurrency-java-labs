package edu.lu.concurrency.week2.day1.part4_race_conditions;

public final class Demo20_JoinDoesNotFixRace {

    private static final class Counter {
        static int count = 0;
    }

    public static void main(String[] args) throws InterruptedException {
        final int threads = Integer.getInteger("demo.threads", 10);
        final int iters   = Integer.getInteger("demo.iters", 100_000);
        final int expected = threads * iters;

        System.out.println("=== Demo20  join() does NOT fix a race ===");

        Counter.count = 0;

        Runnable task = () -> {
            for (int i = 0; i < iters; i++) {
                Counter.count++; // still a race
            }
        };

        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(task, "t" + i);
            ts[i].start();
        }

        // YES we join: all threads finished.
        for (Thread t : ts) t.join();

        System.out.println("[RESULT] expected=" + expected + " actual=" + Counter.count);
        System.out.println("[TAKEAWAY] join() gives visibility + completion, not mutual exclusion.");
    }

    private Demo20_JoinDoesNotFixRace() {}
}