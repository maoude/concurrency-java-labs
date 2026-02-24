package edu.lu.concurrency.week1.lab1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LifecycleSmokeTest {

    @Test
    void threadTerminates() throws Exception {
        Thread t = new Thread(() -> {});
        t.start();
        t.join();
        assertEquals(Thread.State.TERMINATED, t.getState());
    }
}
