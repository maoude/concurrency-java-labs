package edu.lu.concurrency.week2.day1.part2_lifecycle;

public class Demo12_BlockedVsWaiting {

    private static final Object LOCK = new Object();

    public static void main(String[] args) throws Exception {

        // 1) BLOCKED scenario: contender tries to enter synchronized while holder owns LOCK
        Thread holder = new Thread(() -> {
            synchronized (LOCK) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Holder");

        Thread contender = new Thread(() -> {
            synchronized (LOCK) {
                System.out.println("Contender acquired LOCK");
            }
        }, "Contender");

        // 2) WAITING scenario: waiter joins worker (join without timeout)
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Worker");

        Thread waiter = new Thread(() -> {
            try {
                worker.join(); // WAITING
                System.out.println("Waiter resumed after Worker finished");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Waiter");

        holder.start();
        Thread.sleep(100); // ensure holder owns the lock
        contender.start();

        worker.start();
        Thread.sleep(100); // give worker time to start
        waiter.start();

        Thread.sleep(250); // let states settle

        System.out.println("---- Snapshot ----");
        System.out.println("Contender state (expected BLOCKED): " + contender.getState());
        System.out.println("Waiter state    (expected WAITING): " + waiter.getState());

        holder.join();
        contender.join();
        worker.join();
        waiter.join();

        System.out.println("---- Final ----");
        System.out.println("Contender final: " + contender.getState());
        System.out.println("Waiter final   : " + waiter.getState());
    }
}