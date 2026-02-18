public class AutoTuner {
    /**
     * Automatically find optimal thread pool size
     * by running experiments and measuring performance
     */
    public static int findOptimalPoolSize(
            int minSize, int maxSize, int stepSize,
            int targetClients, int requestsPerClient) {
        
        int bestPoolSize = minSize;
        double bestP95 = Double.MAX_VALUE;
        
        System.out.println("Starting auto-tuning...");
        System.out.printf("Testing pool sizes from %d to %d (step %d)%n", 
            minSize, maxSize, stepSize);
        
        for (int poolSize = minSize; poolSize <= maxSize; poolSize += stepSize) {
            System.out.printf("\nTesting pool size: %d%n", poolSize);
            
            // TODO: 
            // 1. Start server with poolSize (use ProcessBuilder or manually)
            // 2. Wait 2 seconds for server startup
            // 3. Run load test with LoadTestClient
            // 4. Parse results to get p95 latency
            // 5. Stop server
            // 6. Track best poolSize
            
            double p95 = runExperiment(poolSize, targetClients, requestsPerClient);
            System.out.printf("Pool size %d → p95 latency: %.2f ms%n", poolSize, p95);
            
            if (p95 < bestP95) {
                bestP95 = p95;
                bestPoolSize = poolSize;
            }
        }
        
        System.out.printf("\n✅ Optimal pool size: %d (p95: %.2f ms)%n", 
            bestPoolSize, bestP95);
        return bestPoolSize;
    }
    
    private static double runExperiment(int poolSize, int clients, int requests) {
        // TODO: Implement experiment runner
        // HINT: Use ProcessBuilder to start/stop server
        // HINT: Parse LoadTestClient CSV output
        return 0.0;
    }
    
    public static void main(String[] args) {
        int optimal = findOptimalPoolSize(
            5,      // min pool size
            100,    // max pool size
            5,      // step size
            50,     // target clients
            10      // requests per client
        );
    }
}
