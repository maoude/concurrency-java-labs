package edu.lu.concurrency.week2.day1.part2_lifecycle;

public class Demo08_JoinVsSleep {

    public static void main(String[] args) throws Exception {

        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            System.out.println("Worker finished");
        }, "Worker");

        worker.start();

        // Uncomment to enforce ordering:
        // worker.join();

        System.out.println("Main finished");
    }
}
