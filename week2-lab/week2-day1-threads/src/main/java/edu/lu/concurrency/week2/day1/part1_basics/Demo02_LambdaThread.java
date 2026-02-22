package edu.lu.concurrency.week2.day1.part1_basics;

public class Demo02_LambdaThread {

    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            Thread current = Thread.currentThread();
            System.out.println("Lambda running on: " + current.getName());
        }, "Lambda-Worker");
        thread.start();
        Thread t2 = Thread.currentThread();
        System.out.println("Running on: " + t2.getName());
    }
}
