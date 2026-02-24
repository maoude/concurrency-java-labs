package edu.lu.concurrency.week1.lab1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StartVsRunTest {

    @Test
    void runDoesNotSpawnNewThread() {
        Thread t = new Thread(() -> {});
        // run() executes on current thread; cannot reliably assert thread name in tests here,
        // but we can at least ensure no exception and thread remains NEW before start().
        assertEquals(Thread.State.NEW, t.getState());
        t.run();
        assertEquals(Thread.State.NEW, t.getState());
    }
}
