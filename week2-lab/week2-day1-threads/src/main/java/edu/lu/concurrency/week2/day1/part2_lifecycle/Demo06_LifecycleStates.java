package edu.lu.concurrency.week2.day1.part2_lifecycle;

public class Demo06_LifecycleStates {

    public static void main(String[] args) throws Exception {

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Lifecycle-Worker");

        System.out.println("State after creation: " + t.getState());

        t.start();
        System.out.println("State after start():  " + t.getState());

        t.join();
        System.out.println("State after join():   " + t.getState());
    }
}
