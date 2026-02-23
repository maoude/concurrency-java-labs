package edu.lu.concurrency.week1.lab1;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * LoadTestClient
 *
 * What it does:
 * - Spawns N concurrent client threads
 * - Each thread sends M HTTP requests to the server
 * - Discards first W requests globally (warmup)
 * - Records latencies (ms) in a thread-safe queue
 * - Prints p50, p95, throughput (req/s)
 * - Optionally exports per-request results to CSV
 *
 * Example usage:
 *   javac LoadTestClient.java
 *   java LoadTestClient --host=127.0.0.1 --port=8080 --clients=50 --requests=20 --warmup=20 --csv=run.csv
 */
public class LoadTestClient {

    // ----------------------------
    // Defaults (reasonable classroom settings)
    // ----------------------------
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_CLIENTS = 10;
    private static final int DEFAULT_REQUESTS = 10;
    private static final int DEFAULT_WARMUP = 20;
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 2000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 5000;

    public static void main(String[] args) throws Exception {

        // ----------------------------
        // Parse CLI args
        // ----------------------------
        Map<String, String> opts = parseArgs(args);

        String host = opts.getOrDefault("host", DEFAULT_HOST);
        int port = parseInt(opts.getOrDefault("port", String.valueOf(DEFAULT_PORT)), "port");
        int clients = parseInt(opts.getOrDefault("clients", String.valueOf(DEFAULT_CLIENTS)), "clients");
        int requestsPerClient = parseInt(opts.getOrDefault("requests", String.valueOf(DEFAULT_REQUESTS)), "requests");
        int warmup = parseInt(opts.getOrDefault("warmup", String.valueOf(DEFAULT_WARMUP)), "warmup");
        String csvPath = opts.get("csv"); // optional

        if (clients <= 0 || requestsPerClient <= 0) {
            throw new IllegalArgumentException("clients and requests must be > 0");
        }
        if (warmup < 0) {
            throw new IllegalArgumentException("warmup must be >= 0");
        }

        int totalRequests = clients * requestsPerClient;

        System.out.println("LoadTestClient");
        System.out.println("==================================================");
        System.out.printf("Target: %s:%d%n", host, port);
        System.out.printf("Clients: %d, Requests/client: %d, Total: %d%n", clients, requestsPerClient, totalRequests);
        System.out.printf("Warmup (discard first): %d requests%n", warmup);
        if (csvPath != null) System.out.printf("CSV output: %s%n", csvPath);
        System.out.println("--------------------------------------------------");

        // ----------------------------
        // Thread-safe collection for latencies (ms)
        // ----------------------------
        ConcurrentLinkedQueue<Long> latenciesMs = new ConcurrentLinkedQueue<>();

        // For optional CSV with per-request lines (timestamp, clientId, reqIndex, latencyMs, status)
        List<String> csvLines = (csvPath != null) ? Collections.synchronizedList(new ArrayList<>()) : null;

        // CountDownLatch to start all threads at the same time (burst)
        CountDownLatch startGate = new CountDownLatch(1);

        // Track when all client threads finish
        CountDownLatch doneGate = new CountDownLatch(clients);

        // Global counter to discard first W measurements (JVM warmup + connection cache effects)
        AtomicLong globalRequestCounter = new AtomicLong(0);

        // Count successful/failed requests
        AtomicLong okCount = new AtomicLong(0);
        AtomicLong failCount = new AtomicLong(0);

        ExecutorService pool = Executors.newFixedThreadPool(clients);

        // Wall-clock window for throughput:
        // - Start right before releasing startGate
        // - End after all doneGate released
        final long startWallNs = System.nanoTime();

        for (int c = 0; c < clients; c++) {
            final int clientId = c;

            pool.submit(() -> {
                try {
                    // Wait until teacher says "GO"
                    startGate.await();

                    for (int i = 0; i < requestsPerClient; i++) {
                        long reqNumber = globalRequestCounter.incrementAndGet();

                        // Measure one request latency (ms)
                        long t0 = System.nanoTime();
                        boolean ok = sendOneHttpRequest(host, port);
                        long t1 = System.nanoTime();

                        long latencyMs = (t1 - t0) / 1_000_000;

                        // Warmup: discard first W requests globally (across all threads)
                        if (reqNumber > warmup) {
                            latenciesMs.add(latencyMs);
                        }

                        if (ok) okCount.incrementAndGet();
                        else failCount.incrementAndGet();

                        // Optional per-request CSV logging
                        if (csvLines != null) {
                            long now = System.currentTimeMillis();
                            // timestamp_ms,client_id,req_index,global_req_no,latency_ms,ok
                            csvLines.add(now + "," + clientId + "," + i + "," + reqNumber + "," + latencyMs + "," + ok);
                        }
                    }
                } catch (Exception e) {
                    // If a client thread dies, count as failures for remaining? Keep it simple:
                    failCount.incrementAndGet();
                    if (csvLines != null) {
                        long now = System.currentTimeMillis();
                        csvLines.add(now + "," + clientId + ",-1,-1,-1,false");
                    }
                } finally {
                    doneGate.countDown();
                }
            });
        }

        // Release all clients simultaneously
        long goNs = System.nanoTime();
        startGate.countDown();

        // Wait for all clients to finish
        doneGate.await();
        long endWallNs = System.nanoTime();

        pool.shutdownNow();

        // ----------------------------
        // Compute metrics
        // ----------------------------
        List<Long> samples = new ArrayList<>(latenciesMs);
        Collections.sort(samples);

        int measured = samples.size();
        int discarded = Math.min(warmup, totalRequests);

        double elapsedSec = (endWallNs - goNs) / 1_000_000_000.0;

        double throughput = (elapsedSec > 0) ? (totalRequests / elapsedSec) : 0.0;

        long p50 = percentile(samples, 50);
        long p95 = percentile(samples, 95);

        System.out.println("Results");
        System.out.println("==================================================");
        System.out.printf("Total requests: %d%n", totalRequests);
        System.out.printf("Discarded (warmup): %d%n", discarded);
        System.out.printf("Measured: %d%n", measured);
        System.out.printf("OK: %d, Failed: %d%n", okCount.get(), failCount.get());
        System.out.printf("Elapsed: %.3f sec%n", elapsedSec);
        System.out.printf("Throughput: %.2f req/s%n", throughput);
        System.out.printf("p50 latency: %d ms%n", p50);
        System.out.printf("p95 latency: %d ms%n", p95);

        // ----------------------------
        // Export CSV (optional)
        // ----------------------------
        if (csvPath != null) {
            writeCsv(csvPath, csvLines, host, port, clients, requestsPerClient, warmup, elapsedSec, throughput, p50, p95);
            System.out.println("--------------------------------------------------");
            System.out.println("CSV written to: " + csvPath);
        }
    }

    /**
     * Sends a single HTTP/1.1 GET request and reads a minimal response.
     * Returns true if a response line is received, false otherwise.
     */
    private static boolean sendOneHttpRequest(String host, int port) {
        try (Socket socket = new Socket(host, port)) {

            socket.setSoTimeout(DEFAULT_READ_TIMEOUT_MS);

            // Send a minimal HTTP request
            OutputStream out = socket.getOutputStream();
            String req =
                    "GET / HTTP/1.1\r\n" +
                    "Host: " + host + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
            out.write(req.getBytes(StandardCharsets.US_ASCII));
            out.flush();

            // Read at least the status line
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            String status = in.readLine(); // e.g. "HTTP/1.1 200 OK"
            return status != null && status.contains("200");

        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Percentile with "nearest-rank" logic (simple, predictable for teaching).
     * For empty list, returns -1.
     */
    private static long percentile(List<Long> sorted, int pct) {
        if (sorted == null || sorted.isEmpty()) return -1;
        if (pct <= 0) return sorted.get(0);
        if (pct >= 100) return sorted.get(sorted.size() - 1);

        // nearest-rank index
        int n = sorted.size();
        int rank = (int) Math.ceil((pct / 100.0) * n);
        int idx = Math.min(Math.max(rank - 1, 0), n - 1);
        return sorted.get(idx);
    }

    /**
     * Minimal CLI parser:
     * Accepts args like:
     *   --clients=50 --requests=20 --warmup=20 --host=127.0.0.1 --port=8080 --csv=run.csv
     */
    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> m = new HashMap<>();
        for (String a : args) {
            if (!a.startsWith("--")) continue;
            String s = a.substring(2);
            int eq = s.indexOf('=');
            if (eq <= 0) {
                // allow flags like --help (not used here)
                m.put(s, "true");
            } else {
                String k = s.substring(0, eq).trim();
                String v = s.substring(eq + 1).trim();
                m.put(k, v);
            }
        }
        return m;
    }

    private static int parseInt(String raw, String name) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer for " + name + ": " + raw);
        }
    }

    /**
     * CSV export:
     * - Writes metadata header as comments (# ...)
     * - Then a standard CSV header line
     * - Then per-request rows
     */
    private static void writeCsv(
            String path,
            List<String> lines,
            String host,
            int port,
            int clients,
            int requestsPerClient,
            int warmup,
            double elapsedSec,
            double throughput,
            long p50,
            long p95
    ) throws IOException {

        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {

            // Metadata (commented)
            pw.println("# host=" + host);
            pw.println("# port=" + port);
            pw.println("# clients=" + clients);
            pw.println("# requestsPerClient=" + requestsPerClient);
            pw.println("# warmupDiscarded=" + warmup);
            pw.printf("# elapsedSec=%.6f%n", elapsedSec);
            pw.printf("# throughput=%.6f%n", throughput);
            pw.println("# p50Ms=" + p50);
            pw.println("# p95Ms=" + p95);

            // Data header
            pw.println("timestamp_ms,client_id,req_index,global_req_no,latency_ms,ok");

            // Data rows
            for (String line : lines) pw.println(line);
        }
    }
}

