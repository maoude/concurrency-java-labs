/**
 * ThreadPoolTuner
 *
 * Implements the lecture formula:
 *
 *   OptimalThreads = Cores × (1 + WaitTime / ComputeTime)
 *
 * Derived from:
 *   - Little's intuition (overlapping waiting)
 *   - Amdahl’s limit (CPU-bound ceiling)
 *
 * This is a heuristic, not a law.
 * It assumes:
 *   - Waiting releases CPU
 *   - Tasks are independent
 *   - No excessive context switching
 */
public class ThreadPoolTuner {

    // Number of available CPU cores
    private static final int CORES =
            Runtime.getRuntime().availableProcessors();

    /**
     * Recommend thread pool size based on wait/compute ratio.
     *
     * @param waitTimeMs     time spent waiting (I/O)
     * @param computeTimeMs  time spent computing (CPU)
     * @return recommended pool size (rounded)
     */
    public static int recommendPoolSize(long waitTimeMs,
                                        long computeTimeMs) {

        // -----------------------------
        // Basic validation
        // -----------------------------
        if (computeTimeMs <= 0) {
            throw new IllegalArgumentException(
                    "computeTimeMs must be > 0");
        }

        if (waitTimeMs < 0) {
            throw new IllegalArgumentException(
                    "waitTimeMs cannot be negative");
        }

        // -----------------------------
        // Core formula
        // -----------------------------
        // optimal = cores × (1 + wait/compute)
        //
        // Interpretation:
        // - If wait is large → many threads useful
        // - If wait is small → near cores only
        double ratio = (double) waitTimeMs / computeTimeMs;

        double rawOptimal = CORES * (1.0 + ratio);

        // Round to nearest integer
        int recommended = (int) Math.round(rawOptimal);

        // -----------------------------
        // Safety cap (engineering realism)
        // -----------------------------
        // Extremely large wait ratios can produce
        // unrealistic thread counts (e.g., 1000+).
        //
        // In practice, OS scheduling & memory
        // limit scalability.
        int maxReasonable = CORES * 50;

        if (recommended > maxReasonable) {
            recommended = maxReasonable;
        }

        // Minimum is 1
        if (recommended < 1) {
            recommended = 1;
        }

        return recommended;
    }

    public static void main(String[] args) {

        System.out.println("System cores: " + CORES);
        System.out.println("\nThread Pool Sizing Recommendations");
        System.out.println("========================================");

        // -------------------------------------------------
        // Scenario 1: Heavy I/O (Typical Web Server)
        // -------------------------------------------------
        long wait1 = 90;
        long compute1 = 10;

        int recommended1 = recommendPoolSize(wait1, compute1);

        System.out.printf(
                "Scenario 1 (Heavy I/O): %dms wait, %dms compute → Pool size: %d%n",
                wait1, compute1, recommended1);

        // Interpretation:
        // Large wait/compute ratio → many threads beneficial

        // -------------------------------------------------
        // Scenario 2: Balanced Workload
        // -------------------------------------------------
        long wait2 = 50;
        long compute2 = 50;

        int recommended2 = recommendPoolSize(wait2, compute2);

        System.out.printf(
                "Scenario 2 (Balanced): %dms wait, %dms compute → Pool size: %d%n",
                wait2, compute2, recommended2);

        // Interpretation:
        // Ratio = 1 → roughly 2 × cores

        // -------------------------------------------------
        // Scenario 3: CPU-bound
        // -------------------------------------------------
        long wait3 = 10;
        long compute3 = 90;

        int recommended3 = recommendPoolSize(wait3, compute3);

        System.out.printf(
                "Scenario 3 (CPU-bound): %dms wait, %dms compute → Pool size: %d%n",
                wait3, compute3, recommended3);

        // Interpretation:
        // Small wait ratio → near core count

        System.out.println("\nKey Insight:");
        System.out.println(" - Heavy I/O → Larger pools make sense.");
        System.out.println(" - CPU-bound → Pool ≈ number of cores.");
        System.out.println(" - More threads than cores does NOT mean more speed.");
    }
}
