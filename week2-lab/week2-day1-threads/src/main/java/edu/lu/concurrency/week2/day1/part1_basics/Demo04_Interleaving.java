package edu.lu.concurrency.week2.day1.part1_basics;

import edu.lu.concurrency.week2.day1.common.Timestamp;

public class Demo04_Interleaving {

    public static void main(String[] args) {

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println(Timestamp.now() + " T1-" + i);
            }
        }, "T1");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println(Timestamp.now() + " T2-" + i);
            }
        }, "T2");

        t1.start();
        t2.start();
    }
}
