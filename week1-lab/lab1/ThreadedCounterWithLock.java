import java.util.concurrent.locks.ReentrantLock;

public class ThreadedCounterWithLock {

    private static final ReentrantLock lock = new ReentrantLock();
    private static long counter = 0;

    public static void main(String[] args) throws Exception {

        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 4;
        int incrementsPerThread = 5_000_000;

        Thread[] workers = new Thread[threads];

        long start = System.nanoTime();

        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    lock.lock();
                    try {
                        counter++;
                    } finally {
                        lock.unlock();
                    }
                }
            });
            workers[i].start();
        }

        for (Thread t : workers) t.join();

        long end = System.nanoTime();

        double seconds = (end - start) / 1_000_000_000.0;
        double throughput = (threads * incrementsPerThread) / seconds;

        System.out.println("Threads: " + threads);
        System.out.println("Throughput: " + throughput + " ops/sec");
    }
}
