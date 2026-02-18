package edu.lu.concurrency.week2.day1.part2_lifecycle;

public class Demo13_SleepDoesNotReleaseLock {

    private static final Object LOCK = new Object();

    public static void main(String[] args) throws Exception {

        Thread sleeper = new Thread(() -> {
            synchronized (LOCK) {
                System.out.println("Sleeper acquired LOCK, now sleeping 3s...");
                try {
                    Thread.sleep(3000); // TIMED_WAITING but LOCK is still held
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Sleeper woke up, releasing LOCK soon.");
            }
        }, "Sleeper");

        Thread blocked = new Thread(() -> {
            System.out.println("BlockedThread trying to acquire LOCK...");
            synchronized (LOCK) {
                System.out.println("BlockedThread acquired LOCK (only after Sleeper exits).");
            }
        }, "BlockedThread");

        sleeper.start();
        Thread.sleep(150); // let sleeper enter synchronized
        blocked.start();

        Thread.sleep(250); // let blocked attempt to enter

        System.out.println("Sleeper state       (expected TIMED_WAITING): " + sleeper.getState());
        System.out.println("BlockedThread state (expected BLOCKED)      : " + blocked.getState());

        sleeper.join();
        blocked.join();

        System.out.println("Final Sleeper       : " + sleeper.getState());
        System.out.println("Final BlockedThread : " + blocked.getState());
    }
}