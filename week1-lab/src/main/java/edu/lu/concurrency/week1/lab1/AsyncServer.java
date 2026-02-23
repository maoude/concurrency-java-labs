package edu.lu.concurrency.week1.lab1;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;


public class AsyncServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Async server listening on port 8080");
        
        while (true) {
            Socket client = serverSocket.accept();
            
            // TODO: Use CompletableFuture for async handling
            CompletableFuture.supplyAsync(() -> {
                try {
                    // Simulate I/O-bound work asynchronously
                    Thread.sleep(100);
                    return "Hello, Async World!";
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept(response -> {
                try {
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    out.println("HTTP/1.1 200 OK\r\n\r\n" + response);
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
