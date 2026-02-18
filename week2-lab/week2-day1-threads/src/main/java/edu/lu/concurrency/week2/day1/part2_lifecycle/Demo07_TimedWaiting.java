package edu.lu.concurrency.week2.day1.part2_lifecycle;

public class Demo07_TimedWaiting {

    public static void main(String[] args) throws Exception {

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Sleeper");

        t.start();
        Thread.sleep(500);

        System.out.println("Observed state: " + t.getState());
    }
}
