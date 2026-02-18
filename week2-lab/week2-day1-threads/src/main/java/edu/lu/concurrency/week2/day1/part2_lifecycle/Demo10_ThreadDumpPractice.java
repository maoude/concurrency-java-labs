package edu.lu.concurrency.week2.day1.part2_lifecycle;

public class Demo10_ThreadDumpPractice {

    private static final Object LOCK = new Object();

    public static void main(String[] args) {

        Thread holder = new Thread(() -> {
            synchronized (LOCK) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Lock-Holder");

        Thread contender = new Thread(() -> {
            synchronized (LOCK) {
                System.out.println("Acquired lock");
            }
        }, "Contender");

        holder.start();
        contender.start();
    }
}
