package edu.lu.concurrency.week2.day1.part3_coordination;

import edu.lu.concurrency.week2.day1.common.Console;

public final class Demo16_SleepDoesNotGuaranteeOrder {

    private static volatile long sink;

    // intentionally NOT volatile
    private static int value = 0;

    public static void main(String[] args) throws InterruptedException {
        Console.hr("Demo16  sleep() does not guarantee you reached the required state");

        final int RUNS = Integer.getInteger("demo.runs", 30);
        final long MAIN_SLEEP_MS = Long.getLong("demo.sleepMs", 1L);

        int sawZero = 0;

    for (int run = 1; run <= RUNS; run++) {
        final int runId = run;   // <-- FIX

        value = 0;

        Thread worker = new Thread(() -> {
            busyWork(3_000_000);
            value = 42;
            System.out.println("[WORKER] run=" + runId + " set value=42");
        }, "worker");


            worker.start();

            // WRONG assumption: "if I sleep a bit, worker must have finished"
            Thread.sleep(MAIN_SLEEP_MS);

            int observed = value;
            boolean alive = worker.isAlive();
            if (observed == 0) sawZero++;

            System.out.println("[RESULT] run=" + run +
                    " mainSleepMs=" + MAIN_SLEEP_MS +
                    " observed=" + observed +
                    " workerAlive=" + alive);

            worker.join(); // cleanup
        }

        System.out.println("[SUMMARY] observedZero=" + sawZero + "/" + RUNS);
        System.out.println("[TAKEAWAY] sleep() does not guarantee ordering relative to another threads progress.");
        System.out.println("[TAKEAWAY] If you need worker finished, you must use join() (or another sync primitive).");
    }

    private static void busyWork(long iters) {
        long x = 0;
        for (long i = 0; i < iters; i++) x = (x * 31) ^ i;
        sink = x;
    }

    private Demo16_SleepDoesNotGuaranteeOrder() {}
}
