package edu.lu.concurrency.week2.day1.part4_race_conditions;

public final class Demo24_ThreadDump_Contention {

    private static final Object LOCK = new Object();

    public static void main(String[] args) throws InterruptedException {
        long pid = ProcessHandle.current().pid();
        System.out.println("=== Demo24  Thread Dump Contention ===");
        System.out.println("PID=" + pid);
        System.out.println("[INSTRUCTION] While running, open another terminal and do:");
        System.out.println("  jps");
        System.out.println("  jstack -l " + pid + " > dump.txt");

        Thread owner = new Thread(() -> {
            synchronized (LOCK) {
                try {
                    Thread.sleep(8_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "owner");

        owner.start();
        Thread.sleep(150);

        for (int i = 0; i < 6; i++) {
            Thread contender = new Thread(() -> {
                synchronized (LOCK) {
                    // acquire and release
                }
            }, "contender-" + i);
            contender.start();
        }

        owner.join();
        System.out.println("[SUMMARY] finished.");
    }

    private Demo24_ThreadDump_Contention() {}
}