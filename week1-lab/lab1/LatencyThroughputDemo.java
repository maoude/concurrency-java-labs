// LatencyThroughputDemo.java
import java.util.*;
import java.util.concurrent.*;

public class LatencyThroughputDemo {
    
    static class WorkloadSimulator {
        private final int poolSize;
        private final long taskDuration;
        private final ExecutorService executor;
        
        public WorkloadSimulator(int poolSize, long taskDuration) {
            this.poolSize = poolSize;
            this.taskDuration = taskDuration;
            this.executor = Executors.newFixedThreadPool(poolSize);
        }
        
        public Metrics runWorkload(int totalTasks) {
            // TODO: Submit totalTasks to the pool
            // TODO: Record start time for each task
            // TODO: Record completion time for each task
            // TODO: Calculate p50, p95 latency
            // TODO: Calculate throughput (tasks/second)
            
            return new Metrics(/* fill in */);
        }
        
        public void shutdown() {
            executor.shutdown();
        }
    }
    
    static class Metrics {
        double p50Latency;
        double p95Latency;
        double throughput;
        
        // TODO: Constructor and toString()
    }
    
    public static void main(String[] args) {
        // TODO: Test combinations:
        // - Small pool + short tasks
        // - Small pool + long tasks
        // - Large pool + short tasks
        // - Large pool + long tasks
        
        // Print comparison table
    }
}