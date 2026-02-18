package edu.lu.concurrency.week2.day1.part2_lifecycle;

public class Demo11_WaitingJoin {

    public static void main(String[] args) throws Exception {

        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Worker");

        Thread waiter = new Thread(() -> {
            try {
                // This thread will enter WAITING until worker terminates
                worker.join();
                System.out.println("Waiter resumed after join()");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Waiter");

        worker.start();
        waiter.start();

        // Give waiter time to reach join()
        Thread.sleep(200);

        System.out.println("Worker state  : " + worker.getState());
        System.out.println("Waiter state  : " + waiter.getState());
        System.out.println("Expected waiter: WAITING (most of the time)");

        worker.join();
        waiter.join();

        System.out.println("Final waiter  : " + waiter.getState());
        System.out.println("Final worker  : " + worker.getState());
    }
}