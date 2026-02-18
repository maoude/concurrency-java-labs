import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class EnhancedLoadClient {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {

        // ----------------------------
        // CLI args (no more editing code)
        // Example:
        // java LoadClient --clients=50 --requests=20 --warmup=20 --csv=run.csv
        // ----------------------------
        Map<String, String> opt = parseArgs(args);

        String host = opt.getOrDefault("host", DEFAULT_HOST);
        int port = Integer.parseInt(opt.getOrDefault("port", String.valueOf(DEFAULT_PORT)));

        int clients = Integer.parseInt(opt.getOrDefault("clients", "50"));
        int requestsPerClient = Integer.parseInt(opt.getOrDefault("requests", "10"));

        int warmupDiscard = Integer.parseInt(opt.getOrDefault("warmup", "20"));
        String csvPath = opt.get("csv"); // optional

        // Latency storage: lock-free queue to avoid contention
        ConcurrentLinkedQueue<Long> latenciesMs = new ConcurrentLinkedQueue<>();

        // Optional per-request CSV lines
        List<String> csvLines = (csvPath != null)
                ? Collections.synchronizedList(new ArrayList<>())
                : null;

        // Synchronize client start to create an actual "burst"
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(clients);

        // Discard first warmup requests globally
        AtomicLong globalCounter = new AtomicLong(0);

        // Count failures (server overload / timeouts / refused connections)
        AtomicLong ok = new AtomicLong(0);
        AtomicLong fail = new AtomicLong(0);

        ExecutorService pool = Executors.newFixedThreadPool(clients);

        System.out.println("LoadClient (metrics)");
        System.out.println("==================================================");
        System.out.printf("Target: %s:%d%n", host, port);
        System.out.printf("clients=%d, requests/client=%d, total=%d%n",
                clients, requestsPerClient, clients * requestsPerClient);
        System.out.printf("warmup discard=%d%n", warmupDiscard);
        if (csvPath != null) System.out.println("csv=" + csvPath);
        System.out.println("--------------------------------------------------");

        // Submit client workers
        for (int c = 0; c < clients; c++) {
            final int clientId = c;
            pool.submit(() -> {
                try {
                    startGate.await();

                    for (int i = 0; i < requestsPerClient; i++) {

                        long reqNo = globalCounter.incrementAndGet();

                        long t0 = System.nanoTime();
                        boolean success = sendOne(host, port);
                        long t1 = System.nanoTime();

                        long latency = (t1 - t0) / 1_000_000;

                        if (success) ok.incrementAndGet();
                        else fail.incrementAndGet();

                        // Warmup discard (globally)
                        if (reqNo > warmupDiscard) {
                            latenciesMs.add(latency);
                        }

                        if (csvLines != null) {
                            long now = System.currentTimeMillis();
                            csvLines.add(now + "," + clientId + "," + i + "," + reqNo + "," + latency + "," + success);
                        }
                    }
                } catch (Exception e) {
                    fail.incrementAndGet();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        // Start burst and measure wall time for throughput
        long startWall = System.nanoTime();
        startGate.countDown();

        doneGate.await();
        long endWall = System.nanoTime();

        pool.shutdownNow();

        // Compute metrics
        double elapsedSec = (endWall - startWall) / 1_000_000_000.0;
        double throughput = (clients * requestsPerClient) / elapsedSec;

        List<Long> samples = new ArrayList<>(latenciesMs);
        Collections.sort(samples);

        long p50 = percentile(samples, 50);
        long p95 = percentile(samples, 95);

        System.out.println("Results");
        System.out.println("==================================================");
        System.out.printf("elapsed=%.3fs%n", elapsedSec);
        System.out.printf("throughput=%.2f req/s%n", throughput);
        System.out.printf("p50=%d ms%n", p50);
        System.out.printf("p95=%d ms%n", p95);
        System.out.printf("ok=%d fail=%d%n", ok.get(), fail.get());

        // CSV export (optional)
        if (csvPath != null) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(csvPath))) {
                pw.println("timestamp_ms,client_id,req_index,global_req_no,latency_ms,ok");
                for (String line : csvLines) pw.println(line);
            }
            System.out.println("CSV written: " + csvPath);
        }
    }

    // Minimal HTTP GET; returns true if response status line exists and contains 200
    private static boolean sendOne(String host, int port) {
        try (Socket socket = new Socket(host, port)) {

            socket.setSoTimeout(5000);

            String req =
                    "GET / HTTP/1.1\r\n" +
                    "Host: " + host + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            socket.getOutputStream().write(req.getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            String status = reader.readLine(); // e.g. HTTP/1.1 200 OK
            return status != null && status.contains("200");

        } catch (Exception e) {
            return false;
        }
    }

    // Nearest-rank percentile on sorted list
    private static long percentile(List<Long> sorted, int pct) {
        if (sorted.isEmpty()) return -1;
        int n = sorted.size();
        int rank = (int) Math.ceil((pct / 100.0) * n);
        int idx = Math.min(Math.max(rank - 1, 0), n - 1);
        return sorted.get(idx);
    }

    // Parse args like --clients=50 --requests=10 --warmup=20 --host=localhost --port=8080 --csv=run.csv
    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> m = new HashMap<>();
        for (String a : args) {
            if (!a.startsWith("--")) continue;
            String s = a.substring(2);
            int eq = s.indexOf('=');
            if (eq > 0) {
                m.put(s.substring(0, eq).trim(), s.substring(eq + 1).trim());
            } else {
                m.put(s.trim(), "true");
            }
        }
        return m;
    }
}
