package edu.lu.concurrency.week2.day1.part3_coordination;

import edu.lu.concurrency.week2.day1.common.Console;

public final class Demo14_SleepVsJoinFailure {

    private static volatile long sink; // prevents JIT dead-code elimination

    public static void main(String[] args) throws InterruptedException {
        Console.hr("Demo14  sleep() used for coordination: statistical failure");

        final int RUNS = Integer.getInteger("demo.runs", 20);
        final int TARGET = 1_000_000;
        final long SLEEP_MS = Long.getLong("demo.sleepMs", 5L);

        int failures = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int run = 1; run <= RUNS; run++) {
            Counter.value = 0;

            Thread worker = new Thread(() -> {
                busyWork(2_000_000); // add variability
                for (int i = 0; i < TARGET; i++) {
                    Counter.value++;
                }
            }, "worker");

            worker.start();

            // WRONG: time-guessing instead of state-checking
            Thread.sleep(SLEEP_MS);

            int observed = Counter.value;
            boolean alive = worker.isAlive();

            min = Math.min(min, observed);
            max = Math.max(max, observed);

            if (observed != TARGET) failures++;

            System.out.println("[RESULT] run=" + run +
                    " sleepMs=" + SLEEP_MS +
                    " counter=" + observed +
                    " workerAlive=" + alive);

            // cleanup so runs don't overlap
            worker.join();
        }

        System.out.println("[SUMMARY] failures=" + failures + "/" + RUNS +
                " min=" + min + " max=" + max + " target=" + TARGET);

        System.out.println("[TAKEAWAY] sleep() coordinates TIME, not COMPLETION.");
        System.out.println("[TAKEAWAY] If correctness depends on completion, sleep() is the wrong tool.");
    }

    private static void busyWork(long iters) {
        long x = 0;
        for (long i = 0; i < iters; i++) x = (x * 31) ^ i;
        sink = x;
    }

    private static final class Counter {
        static int value = 0;
    }

    private Demo14_SleepVsJoinFailure() {}
}
