package edu.lu.concurrency.week1.lab1;

import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MultiThreadedServer {

    // Default pool size for the demo; can be overridden via args.
    private static final int DEFAULT_POOL_SIZE = 10;

    public static void main(String[] args) throws IOException {

        // Allow: java MultiThreadedServer 20
        int poolSize = DEFAULT_POOL_SIZE;
        if (args.length >= 1) {
            try {
                poolSize = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                // Keep default if parsing fails
            }
        }

        // Fixed thread pool:
        // - Reuses threads (avoids thread creation cost per request)
        // - Limits concurrency (prevents creating 1000 threads under load)
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);

        // Shutdown hook ensures the pool is stopped when you terminate the program (Ctrl+C)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down thread pool...");
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

        // try-with-resources ensures the ServerSocket closes cleanly
        try (ServerSocket serverSocket = new ServerSocket()) {

            // Makes restarting the server easier (port reuse)
            serverSocket.setReuseAddress(true);

            // Bind to port 8080
            serverSocket.bind(new InetSocketAddress(8080));

            System.out.println("Multi-threaded server (pool=" + poolSize + ") listening on port 8080");

            // Main accept loop:
            // - accept() blocks until a client connects
            // - when connected, we submit request handling to the pool
            while (true) {
                final Socket client = serverSocket.accept();

                // Submit request handling to the pool.
                // Key benefit: while one request is sleeping (I/O wait),
                // other threads can serve other clients.
                pool.execute(() -> handleRequestSafely(client));
            }
        }
    }

    // Wrapper so the pool thread never dies due to an uncaught exception.
    private static void handleRequestSafely(Socket client) {
        try {
            handleRequest(client);
        } catch (Exception e) {
            // Under high load, printing stack traces kills performance and floods output.
            // Keep it short for demo reliability.
            System.err.println("Request failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    private static void handleRequest(Socket client) throws IOException {
        // try-with-resources closes the socket automatically even on exceptions
        try (client) {

            long startTime = System.nanoTime();

            // ---------------------------------------------------------
            // SIMULATED I/O WAIT (DB/API/network)
            // ---------------------------------------------------------
            // During sleep:
            // - this worker thread is blocked
            // - BUT other threads in the pool can still process requests
            // This is exactly why latency collapses under load compared to single-threaded.
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // ---------------------------------------------------------
            // Proper minimal HTTP response (demo-safe)
            // ---------------------------------------------------------
            String body = "Hello, World!\n";
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

            long endTime = System.nanoTime();
            long latencyMs = (endTime - startTime) / 1_000_000;

            // Log includes thread name to prove concurrency in action
            System.out.printf("[%d] latency=%d ms handled-by=%s%n",
                    System.currentTimeMillis(), latencyMs, Thread.currentThread().getName());
        }
    }
}

