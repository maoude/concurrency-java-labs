package edu.lu.concurrency.week1.lab1;

// IOProfiler.java

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class IOProfiler {

    private static final int CORES =
            Runtime.getRuntime().availableProcessors();

    /**
     * Profile a workload and measure:
     *  - Wall clock time (real elapsed time)
     *  - CPU time (actual compute time used by thread)
     *  - Derived wait time
     *
     * Wall time = total elapsed time
     * CPU time  = actual time executing on CPU
     * Wait time = Wall - CPU
     */
    public static void profileTask(String label, Runnable task) {

        ThreadMXBean bean = ManagementFactory.getThreadMXBean();

        // Make sure CPU time measurement is enabled
        if (!bean.isThreadCpuTimeEnabled()) {
            bean.setThreadCpuTimeEnabled(true);
        }

        long startCpu = bean.getCurrentThreadCpuTime();
        long startWall = System.nanoTime();

        // Execute the workload
        task.run();

        long endWall = System.nanoTime();
        long endCpu = bean.getCurrentThreadCpuTime();

        long wallTimeMs = (endWall - startWall) / 1_000_000;
        long cpuTimeMs  = (endCpu - startCpu) / 1_000_000;

        long waitTimeMs = wallTimeMs - cpuTimeMs;

        // Prevent divide by zero (pure CPU tasks may have wait ~ 0)
        double ratio = cpuTimeMs == 0 ? 0 :
                (double) waitTimeMs / cpuTimeMs;

        System.out.println("========================================");
        System.out.println("Workload: " + label);
        System.out.printf("Wall time: %d ms%n", wallTimeMs);
        System.out.printf("CPU time : %d ms%n", cpuTimeMs);
        System.out.printf("Wait time: %d ms%n", waitTimeMs);
        System.out.printf("Wait/Compute ratio: %.2f%n", ratio);

        int recommendedPool =
                recommendPoolSize(waitTimeMs, cpuTimeMs);

        System.out.printf("Recommended thread pool size: %d%n",
                recommendedPool);
    }

    /**
     * Thread pool sizing formula from lecture:
     *
     * OptimalThreads = cores Ã— (1 + wait/compute)
     *
     * Intuition:
     * - If wait >> compute â†’ large pool
     * - If compute >> wait â†’ near core count
     */
    public static int recommendPoolSize(long waitMs, long computeMs) {

        if (computeMs <= 0) {
            return CORES; // fallback
        }

        double ratio = (double) waitMs / computeMs;

        double optimal =
                CORES * (1 + ratio);

        return (int) Math.round(optimal);
    }

    /**
     * Intentionally slow recursive Fibonacci
     * Used to simulate CPU-bound workload.
     */
    private static int fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    public static void main(String[] args) {

        System.out.println("System cores: " + CORES);

        // ----------------------------------------------------
        // 1) Pure I/O simulation (Thread.sleep)
        // ----------------------------------------------------
        profileTask("Pure I/O (Thread.sleep 200ms)", () -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // ----------------------------------------------------
        // 2) Pure CPU workload (Fibonacci)
        // ----------------------------------------------------
        profileTask("Pure CPU (Fibonacci 40)", () -> {
            fibonacci(40); // heavy compute
        });

        // ----------------------------------------------------
        // 3) Mixed workload (I/O + CPU)
        // ----------------------------------------------------
        profileTask("Mixed (Sleep 100ms + Fibonacci 38)", () -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            fibonacci(38);
        });
    }
}

