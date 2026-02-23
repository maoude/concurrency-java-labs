package edu.lu.concurrency.week1.lab1;

import java.util.LinkedList;
import java.util.Queue;

public class QueueingLittleLawDemo {

    public static void main(String[] args) throws Exception {

        double arrivalRate = 50; // requests per second
        double serviceTimeMs = 100; // ms per request

        Queue<Long> queue = new LinkedList<>();

        long simulationStart = System.currentTimeMillis();
        long lastArrival = simulationStart;
        long processed = 0;

        while (System.currentTimeMillis() - simulationStart < 5000) {

            long now = System.currentTimeMillis();

            // Simulate arrivals
            if (now - lastArrival >= 1000 / arrivalRate) {
                queue.add(now);
                lastArrival = now;
            }

            // Simulate service
            if (!queue.isEmpty()) {
                long arrival = queue.poll();
                Thread.sleep((long) serviceTimeMs);
                processed++;
            }
        }

        System.out.println("Processed: " + processed);
        System.out.println("Remaining in queue: " + queue.size());
    }
}

