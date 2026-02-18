package edu.lu.concurrency.week2.day1.part1_basics;

public class Demo05_SleepMyth {

    public static void main(String[] args) throws InterruptedException {

        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println("T1 finished");
        });

        Thread t2 = new Thread(() -> {
            System.out.println("T2 finished");
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("Main finished");
    }
}
