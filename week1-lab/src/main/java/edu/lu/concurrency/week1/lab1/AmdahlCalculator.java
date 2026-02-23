package edu.lu.concurrency.week1.lab1;

public class AmdahlCalculator {

    /**
     * Compute theoretical speedup using Amdahl's Law.
     *
     * Formula:
     *   Speedup = 1 / ((1 - P) + (P / N))
     *
     * where:
     *   P = fraction of the program that can be parallelized
     *   N = number of cores (or parallel workers)
     *
     * This gives the *maximum theoretical speedup*.
     */
    public static double calculateSpeedup(double parallelFraction, int cores) {

        // Defensive programming:
        // Parallel fraction must be between 0 and 1.
        if (parallelFraction < 0 || parallelFraction > 1) {
            throw new IllegalArgumentException("parallelFraction must be between 0 and 1");
        }

        // You cannot have zero or negative cores.
        if (cores <= 0) {
            throw new IllegalArgumentException("cores must be > 0");
        }

        // Direct implementation of Amdahl's Law:
        // (1 - P)  = sequential portion (cannot be parallelized)
        // (P / N)  = parallel portion divided among N cores
        return 1.0 / ((1.0 - parallelFraction) + (parallelFraction / cores));
    }

    public static void main(String[] args) {

        // Different fractions of parallelizable work.
        // 0.50 = 50%, 0.75 = 75%, etc.
        double[] parallelPercentages = {0.50, 0.75, 0.90, 0.95};

        // Different core counts to test scaling behavior.
        // Integer.MAX_VALUE is used to simulate "infinite cores".
        int[] coreCounts = {2, 4, 8, 16, Integer.MAX_VALUE};

        System.out.println("Amdahl's Law Speedup Table");
        System.out.println("========================================");

        // Print table header
        System.out.printf("%-12s", "Parallel %");

        for (int cores : coreCounts) {
            if (cores == Integer.MAX_VALUE) {
                // Symbolic representation of infinite cores
                System.out.printf("%-12s", "âˆž cores");
            } else {
                System.out.printf("%-12s", cores + " cores");
            }
        }
        System.out.println();

        // For each parallel fraction (P)
        for (double p : parallelPercentages) {

            // Print the percentage in readable format (e.g., 50%)
            System.out.printf("%-12.0f%%", p * 100);

            // For each core count (N)
            for (int cores : coreCounts) {

                // Replace "infinite cores" with a very large number.
                // This approximates the theoretical limit:
                // Speedup â†’ 1 / (1 - P)
                int effectiveCores = (cores == Integer.MAX_VALUE) ? 1_000_000 : cores;

                // Compute theoretical speedup
                double speedup = calculateSpeedup(p, effectiveCores);

                // Print with 2 decimal precision
                System.out.printf("%-12.2f", speedup);
            }

            // Move to next row
            System.out.println();
        }

        /*
         * What students should observe:
         *
         * 1) Speedup increases as cores increase.
         * 2) Speedup eventually plateaus.
         * 3) The plateau equals 1 / (1 - P).
         *
         * This demonstrates:
         *   More cores â‰  infinite scaling.
         *   The sequential part limits everything.
         */
    }
}

