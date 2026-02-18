import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CPUBoundServer
 *
 * Same structure as MultiThreadedServer,
 * but replaces I/O wait with heavy CPU computation.
 *
 * Pedagogical purpose:
 * Show that increasing threads does NOT improve performance
 * when workload is CPU-bound.
 */
public class CPUBoundServer {

    // Default thread pool size (can override via command-line)
    private static final int DEFAULT_POOL_SIZE = 10;

    // Default workload intensity (Fibonacci input)
    private static int DEFAULT_FIB_N = 35;

    public static void main(String[] args) throws IOException {

    int poolSize = DEFAULT_POOL_SIZE;
    int fibNTemp = DEFAULT_FIB_N;

    if (args.length >= 1) {
        poolSize = Integer.parseInt(args[0]);
    }
    if (args.length >= 2) {
        fibNTemp = Integer.parseInt(args[1]);
    }

    // Now assign once to final variable
    final int fibN = fibNTemp;


        ExecutorService pool = Executors.newFixedThreadPool(poolSize);

        // Graceful shutdown when CTRL+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down CPU-bound server...");
            pool.shutdown();
            try {
                if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));

        try (ServerSocket serverSocket = new ServerSocket()) {

            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(8080));

            System.out.println("CPU-bound server (pool=" + poolSize +
                    ", fibN=" + fibN + ") listening on port 8080");

            while (true) {
                final Socket client = serverSocket.accept();

                // Submit CPU-heavy task to thread pool
                pool.execute(() -> handleRequestSafely(client, fibN));
            }
        }
    }

    /**
     * Wrapper to prevent worker thread from dying silently.
     */
    private static void handleRequestSafely(Socket client, int fibN) {
        try {
            handleRequest(client, fibN);
        } catch (Exception e) {
            System.err.println("Request failed: " + e.getMessage());
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * Handle a single request.
     *
     * This version performs CPU-heavy computation instead of sleeping.
     */
    private static void handleRequest(Socket client, int fibN) throws IOException {

        try (client) {

            long startTime = System.nanoTime();

            // ---------------------------------------------------------
            // CPU-INTENSIVE TASK
            // ---------------------------------------------------------
            // Recursive Fibonacci is intentionally inefficient:
            // - No memoization
            // - Exponential time complexity
            //
            // This saturates CPU cores.
            //
            // Key pedagogical point:
            // Unlike sleep(), this does NOT release CPU.
            // Threads compete for CPU time.
            int result = fibonacci(fibN);

            long endTime = System.nanoTime();
            long latencyMs = (endTime - startTime) / 1_000_000;

            // ---------------------------------------------------------
            // Proper HTTP Response
            // ---------------------------------------------------------
            String body = "Result: " + result + "\n";
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

            String headers =
                    "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain; charset=utf-8\r\n" +
                    "Content-Length: " + bodyBytes.length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            OutputStream out = client.getOutputStream();
            out.write(headers.getBytes(StandardCharsets.US_ASCII));
            out.write(bodyBytes);
            out.flush();

            // Log request metrics
            System.out.printf("[%d] CPU-bound latency=%d ms handled-by=%s%n",
                    System.currentTimeMillis(),
                    latencyMs,
                    Thread.currentThread().getName());
        }
    }

    /**
     * Inefficient recursive Fibonacci (intentionally slow).
     *
     * Time complexity: O(2^n)
     *
     * This ensures CPU saturation for demonstration purposes.
     */
    private static int fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
}
