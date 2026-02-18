package edu.lu.concurrency.week2.day1.extra;

public class YieldExperiment {

    public static void main(String[] args) {

        Runnable yielder = () -> {
            for (int i = 0; i < 20; i++) {
                System.out.println("Yielding thread step " + i);
                Thread.yield(); // Hint only
            }
        };

        Runnable normal = () -> {
            for (int i = 0; i < 20; i++) {
                System.out.println("Normal thread step " + i);
            }
        };

        new Thread(yielder, "Yielder").start();
        new Thread(normal, "Normal").start();
    }
}
