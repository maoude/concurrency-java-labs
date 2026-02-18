// SingleThreadedServer.java
// Demonstrates a blocking, sequential server architecture.
// Used to illustrate I/O-bound bottlenecks and queue formation.
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
public class SingleThreadedServer {
    public static void main(String[] args) throws IOException {
        // Create a ServerSocket (TCP listener)
        // This binds to port 8080 and waits for incoming connections.
        try (ServerSocket serverSocket = new ServerSocket()) {
            // Allow immediate reuse of the port if the server restarts
            serverSocket.setReuseAddress(true);
            // Bind the server to port 8080
            serverSocket.bind(new InetSocketAddress(8080));
            System.out.println("Single-threaded server listening on port 8080");
            // Infinite loop: server runs forever
            while (true) {
                // BLOCKING CALL:
                // accept() waits until a client connects.
                // During this time, the thread is idle.
                Socket client = serverSocket.accept();
                // IMPORTANT:
                // This is where the architecture becomes sequential.
                // The server will not accept another client
                // until handleRequest() finishes.
                handleRequest(client);
            }
        }
    }
    private static void handleRequest(Socket client) throws IOException {
        // try-with-resources ensures socket closes automatically
        try (client) {
            // ---------------------------------------------------------
            // SIMULATED I/O DELAY
            // ---------------------------------------------------------
            // Thread.sleep(100) simulates:
            // - Database query
            // - External API call
            // - Disk read
            //
            // During this 100ms:
            // - CPU is NOT computing
            // - Thread is BLOCKED
            // - No other client can be served
            //
            // This is the core of the "waiting problem".
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // ---------------------------------------------------------
            // RESPONSE BODY
            // ---------------------------------------------------------
            String body = "Hello, World!\n";
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            // ---------------------------------------------------------
            // HTTP RESPONSE HEADERS
            // ---------------------------------------------------------
            // Content-Length is required so clients know when body ends.
            // Connection: close ensures we terminate the socket.
            String headers =
                    "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain; charset=utf-8\r\n" +
                    "Content-Length: " + bodyBytes.length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
            // ---------------------------------------------------------
            // SEND RESPONSE
            // ---------------------------------------------------------
            // Writing headers + body to the client.
            OutputStream out = client.getOutputStream();
            out.write(headers.getBytes(StandardCharsets.US_ASCII));
            out.write(bodyBytes);
            // flush() forces data to be sent immediately
            out.flush();
            // Socket closes automatically after this block
        }
    }
}
