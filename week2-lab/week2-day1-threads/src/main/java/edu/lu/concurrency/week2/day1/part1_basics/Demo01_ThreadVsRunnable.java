package edu.lu.concurrency.week2.day1.part1_basics;
public class Demo01_ThreadVsRunnable {
    static class MyTask implements Runnable {
        @Override
        public void run() {
            Thread t = Thread.currentThread();
            System.out.println("Runnable executed by: " + t.getName());
        }
    }
    public static void main(String[] args) {
        Thread thread = new Thread(new MyTask(), "Worker-1");
        thread.start();
        Thread t2 = Thread.currentThread();
        System.out.println("Runnable executed by: " + t2.getName());
    }
}
