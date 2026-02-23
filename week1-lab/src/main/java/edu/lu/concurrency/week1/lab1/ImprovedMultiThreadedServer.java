package edu.lu.concurrency.week1.lab1;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ImprovedMultiThreadedServer {

    // Tune these in class to demonstrate "sweet spot"
    private static final int POOL_SIZE = 20;

    // Bounded queue prevents memory explosion under overload
    private static final int QUEUE_CAPACITY = 100;

    // If a request waits too long in the queue, we drop it (basic admission control)
    private static final long QUEUE_TIMEOUT_MS = 1000;

    // Protect worker threads from slow clients hanging on reads
    private static final int SOCKET_READ_TIMEOUT_MS = 2000;

    private static final AtomicLong rejectedCount = new AtomicLong(0);

    public static void main(String[] args) throws IOException {

        // Optional: allow port via args
        int port = 8080;
        if (args.length >= 1) port = Integer.parseInt(args[0]);

        // ---- Bounded work queue (core overload protection) ----
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

        // Custom rejection handler: backpressure + visible logging
        RejectedExecutionHandler rejectionHandler = (r, executor) -> {
            rejectedCount.incrementAndGet();
            // Backpressure strategy:
            // CallerRunsPolicy runs task on the accept thread, slowing down accept() => "push back" on load.
            if (!executor.isShutdown()) {
                r.run();
            }
        };

        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                POOL_SIZE, POOL_SIZE,
                30L, TimeUnit.SECONDS,
                queue,
                rejectionHandler
        );

        // Prestart threads so first burst doesn't pay thread-creation latency
        pool.prestartAllCoreThreads();

        // ---- Server socket (use try-with-resources style control) ----
        final ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(port));

        System.out.println("Improved server listening on port " + port +
                " (pool=" + POOL_SIZE + ", queue=" + QUEUE_CAPACITY + ")");

        // ---- Graceful shutdown hook (closes accept() + stops pool) ----
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down gracefully...");
            try {
                serverSocket.close(); // IMPORTANT: unblocks accept()
            } catch (IOException ignored) {}

            pool.shutdown();
            try {
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }

            System.out.println("Rejected tasks (overload events): " + rejectedCount.get());
        }));

        // ---- Accept loop ----
        while (true) {
            Socket client;
            try {
                client = serverSocket.accept();
            } catch (IOException e) {
                // Happens when serverSocket is closed during shutdown
                break;
            }

            // Protect worker threads from hanging reads
            client.setSoTimeout(SOCKET_READ_TIMEOUT_MS);

            // Warn when queue is near full (overload approaching)
            int qSize = queue.size();
            if (qSize >= (int) (QUEUE_CAPACITY * 0.80)) {
                System.err.printf("WARNING: Queue %.0f%% full (%d/%d)%n",
                        (qSize * 100.0 / QUEUE_CAPACITY), qSize, QUEUE_CAPACITY);
            }

            final long enqueueTimeMs = System.currentTimeMillis();

            // Submit a wrapper task that:
            // - checks queue wait time
            // - always closes the socket
            pool.execute(() -> handleRequestSafely(client, enqueueTimeMs));
        }
    }

    private static void handleRequestSafely(Socket client, long enqueueTimeMs) {
        try (Socket c = client) { // ensures close no matter what
            long queueWaitMs = System.currentTimeMillis() - enqueueTimeMs;

            // Admission control: if it waited too long, drop it
            if (queueWaitMs > QUEUE_TIMEOUT_MS) {
                System.err.println("Dropped request (queue timeout): " + queueWaitMs + "ms");
                return;
            }

            handleRequest(c);

        } catch (IOException e) {
            // For classroom: print once; in real systems you'd structure logging
            System.err.println("I/O error: " + e.getMessage());
        }
    }

    // Same core logic as before (I/O-bound simulation)
    private static void handleRequest(Socket client) throws IOException {
        long start = System.nanoTime();

        try {
            // Simulate I/O wait (DB, network)
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Connection: close\r\n");
            out.print("\r\n");
            out.print("OK\n");

        } finally {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            System.out.printf("[%d] latency=%dms thread=%s%n",
                    System.currentTimeMillis(), latencyMs, Thread.currentThread().getName());
        }
    }
}

