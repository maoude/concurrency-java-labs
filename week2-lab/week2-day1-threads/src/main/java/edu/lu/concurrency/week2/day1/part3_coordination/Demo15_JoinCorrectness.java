package edu.lu.concurrency.week2.day1.part3_coordination;

import edu.lu.concurrency.week2.day1.common.Console;

public final class Demo15_JoinCorrectness {

    private static volatile long sink;

    public static void main(String[] args) throws InterruptedException {
        Console.hr("Demo15  join() guarantees completion: correct results");

        final int RUNS = Integer.getInteger("demo.runs", 10);
        final int TARGET = 1_000_000;

        int failures = 0;

        for (int run = 1; run <= RUNS; run++) {
            Counter.value = 0;

            Thread worker = new Thread(() -> {
                busyWork(2_000_000);
                for (int i = 0; i < TARGET; i++) {
                    Counter.value++;
                }
            }, "worker");

            worker.start();

            long t0 = System.nanoTime();
            worker.join(); // correct: wait for state (termination)
            long waitedMs = (System.nanoTime() - t0) / 1_000_000;

            int observed = Counter.value;

            if (observed != TARGET) failures++;

            System.out.println("[RESULT] run=" + run +
                    " waitedMs=" + waitedMs +
                    " counter=" + observed +
                    " workerAlive=" + worker.isAlive());
        }

        System.out.println("[SUMMARY] failures=" + failures + "/" + RUNS +
                " target=" + TARGET);

        System.out.println("[TAKEAWAY] join() coordinates COMPLETION and establishes happens-before.");
    }

    private static void busyWork(long iters) {
        long x = 0;
        for (long i = 0; i < iters; i++) x = (x * 31) ^ i;
        sink = x;
    }

    private static final class Counter {
        static int value = 0;
    }

    private Demo15_JoinCorrectness() {}
}
