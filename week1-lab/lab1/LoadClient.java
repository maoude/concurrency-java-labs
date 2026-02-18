// LoadClient.java
// Sends concurrent HTTP requests to localhost:8080
// Used to demonstrate queue formation in single-threaded server
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
public class LoadClient {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        int concurrentClients = 50; // change to 1, 5, 50
        CountDownLatch latch = new CountDownLatch(concurrentClients);
        List<Long> responseTimes = new ArrayList<>();
        long globalStart = System.currentTimeMillis();
        for (int i = 0; i < concurrentClients; i++) {
            new Thread(() -> {
                try {
                    long start = System.currentTimeMillis();
                    try (Socket socket = new Socket(HOST, PORT)) {
                        // Send minimal HTTP request
                        socket.getOutputStream().write(
                                "GET / HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes()
                        );
                        // Read response
                        BufferedReader reader =
                                new BufferedReader(
                                        new InputStreamReader(socket.getInputStream())
                                );

                        while (reader.readLine() != null) {
                            // consume response
                        }
                    }
                    long end = System.currentTimeMillis();
                    synchronized (responseTimes) {
                        responseTimes.add(end - start);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        long globalEnd = System.currentTimeMillis();
        double avg = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
        System.out.println("Concurrent Clients: " + concurrentClients);
        System.out.println("Average Response Time: " + avg + " ms");
        System.out.println("Total Time: " + (globalEnd - globalStart) + " ms");
    }
}
