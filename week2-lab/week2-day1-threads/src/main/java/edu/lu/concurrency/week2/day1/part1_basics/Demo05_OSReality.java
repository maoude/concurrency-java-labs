package edu.lu.concurrency.week2.day1.part1_basics;

public class Demo05_OSReality {

    public static void main(String[] args) {

        for (int run = 1; run <= 20; run++) {
            final int r = run;
            Thread t = new Thread(() ->
                System.out.println("Run " + r + " executed by " + Thread.currentThread().getName())
            );
            t.start();
        }
    }
}
