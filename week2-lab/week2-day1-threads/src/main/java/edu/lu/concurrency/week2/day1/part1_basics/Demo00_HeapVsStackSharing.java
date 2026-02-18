package edu.lu.concurrency.week2.day1.part1_basics;

public class Demo00_HeapVsStackSharing {

    static class Counter {
        int value = 0;
    }

    public static void main(String[] args) throws InterruptedException {

        Counter sharedCounter = new Counter(); // Heap object

        Runnable task = () -> {

            // Local variable (stack)
            int localCopy = sharedCounter.value;

            for (int i = 0; i < 5; i++) {
                sharedCounter.value++;
                System.out.println(
                        Thread.currentThread().getName()
                        + " | localCopy=" + localCopy
                        + " | shared=" + sharedCounter.value
                );
            }
        };

        Thread t1 = new Thread(task, "T1");
        Thread t2 = new Thread(task, "T2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("Final shared value: " + sharedCounter.value);
    }
}
