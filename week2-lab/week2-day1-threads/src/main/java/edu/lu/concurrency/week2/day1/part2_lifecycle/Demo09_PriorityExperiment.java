package edu.lu.concurrency.week2.day1.part2_lifecycle;

import java.util.concurrent.CountDownLatch;
import java.time.Instant;

public class Demo09_PriorityExperiment {

    public static void main(String[] args) throws Exception {

        for (int run = 1; run <= 10; run++) {

            CountDownLatch start = new CountDownLatch(1);

            Runnable task = () -> {
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                Thread t = Thread.currentThread();
                System.out.println(
                    Instant.now()
                        + " Finished: " + t.getName()
                        + " | Priority: " + t.getPriority()
                );
            };

            Thread low = new Thread(task, "Low");
            Thread high = new Thread(task, "High");

            low.setPriority(Thread.MIN_PRIORITY);
            high.setPriority(Thread.MAX_PRIORITY);

            low.start();
            high.start();

            start.countDown();

            low.join();
            high.join();

            System.out.println("---- Run " + run + " ----\n");
        }
    }
}
