package edu.lu.concurrency.week1.lab1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ThreadedCounterWithLockTest {
    @Test void runsAndPrints() throws Exception {
        String out = TestConsole.captureStdout(() -> ThreadedCounterWithLock.main(new String[0]));
        assertFalse(out.trim().isEmpty());
    }
}