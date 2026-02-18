package edu.lu.concurrency.week2.day1.part1_basics;

public class Demo03_StartVsRun {

    public static void main(String[] args) {

        Thread t = new Thread(() ->
            System.out.println("Running in: " + Thread.currentThread().getName())
        );

        System.out.println("Calling run()");
        t.run();

        System.out.println("Calling start()");
        t.start();
    }
}
