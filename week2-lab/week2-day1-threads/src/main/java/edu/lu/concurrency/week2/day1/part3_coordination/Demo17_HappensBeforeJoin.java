package edu.lu.concurrency.week2.day1.part3_coordination;

import edu.lu.concurrency.week2.day1.common.Console;

public final class Demo17_HappensBeforeJoin {

    // intentionally NOT volatile
    private static int value = 0;
    private static boolean done = false;

    public static void main(String[] args) throws InterruptedException {
        Console.hr("Demo17  happens-before via join(): visibility without volatile");

        final int RUNS = Integer.getInteger("demo.runs", 20);
        int failures = 0;

        for (int run = 1; run <= RUNS; run++) {
            value = 0;
            done = false;

            Thread worker = new Thread(() -> {
                value = 42;
                done = true;
            }, "worker");

            worker.start();

            // join() establishes happens-before for thread termination.
            worker.join();

            int v = value;
            boolean d = done;

            boolean ok = (v == 42 && d);
            if (!ok) failures++;

            System.out.println("[RESULT] run=" + run + " value=" + v + " done=" + d + " ok=" + ok);
        }

        System.out.println("[SUMMARY] failures=" + failures + "/" + RUNS);
        System.out.println("[TAKEAWAY] join() => all worker writes become visible after join returns (happens-before).");
    }

    private Demo17_HappensBeforeJoin() {}
}
