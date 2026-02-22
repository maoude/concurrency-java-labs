package edu.lu.concurrency.week2.day1.part3_coordination;

import edu.lu.concurrency.week2.day1.common.Console;

public final class Demo18_JoinTimeoutPitfall {

    public static void main(String[] args) throws InterruptedException {
        Console.hr("Demo18  join(timeout) does NOT guarantee completion");

        final int RUNS = Integer.getInteger("demo.runs", 15);
        final long JOIN_TIMEOUT_MS = Long.getLong("demo.joinTimeoutMs", 500L);

        // Two scenarios: worker longer than timeout AND worker shorter than timeout
        runScenario("Worker longer than timeout", 2000, JOIN_TIMEOUT_MS, RUNS);
        runScenario("Worker shorter than timeout", 200, JOIN_TIMEOUT_MS, RUNS);
    }

    private static void runScenario(String description, long workerSleepMs, long joinTimeoutMs, int runs)
            throws InterruptedException {

        Console.hr(description + " (workerSleepMs=" + workerSleepMs + ", joinTimeoutMs=" + joinTimeoutMs + ")");
        int completedWithinTimeout = 0;

        for (int run = 1; run <= runs; run++) {

            Thread worker = new Thread(() -> {
                try {
                    Thread.sleep(workerSleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("[WORKER] interrupted");
                    return;
                }
                System.out.println("[WORKER] done");
            }, "worker");

            worker.start();

            long t0 = System.nanoTime();
            worker.join(joinTimeoutMs);
            long waitedMs = (System.nanoTime() - t0) / 1_000_000;

            boolean alive = worker.isAlive();
            if (!alive) completedWithinTimeout++;

            System.out.println("[RESULT] run=" + run +
                    " waitedMs=" + waitedMs +
                    " workerAlive=" + alive);

            // Always clean up so next run is isolated
            worker.join();
        }

        System.out.println("[SUMMARY] completedWithinTimeout=" + completedWithinTimeout + "/" + runs);
        System.out.println("[TAKEAWAY] join(timeout) means: wait UP TO timeout. It is not a completion guarantee.");
    }

    private Demo18_JoinTimeoutPitfall() {}
}
