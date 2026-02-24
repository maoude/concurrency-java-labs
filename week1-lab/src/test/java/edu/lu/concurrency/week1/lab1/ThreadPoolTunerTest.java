package edu.lu.concurrency.week1.lab1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThreadPoolTunerTest {

    @Test
    void recommendPoolSize_validatesInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> ThreadPoolTuner.recommendPoolSize(0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> ThreadPoolTuner.recommendPoolSize(10, 0));
        assertThrows(IllegalArgumentException.class,
                () -> ThreadPoolTuner.recommendPoolSize(-1, 10));
    }

    @Test
    void recommendPoolSize_matchesFormulaNearCores() {
        int cores = Runtime.getRuntime().availableProcessors();

        // wait=0 => should be around cores (rounded)
        int r0 = ThreadPoolTuner.recommendPoolSize(0, 10);
        assertTrue(r0 >= 1);
        assertTrue(Math.abs(r0 - cores) <= 1, "Expected near cores");

        // wait=compute => around cores * (1 + 1) = 2*cores
        int r1 = ThreadPoolTuner.recommendPoolSize(10, 10);
        assertTrue(r1 >= cores, "Expected >= cores when wait=compute");
        assertTrue(Math.abs(r1 - (2 * cores)) <= 2, "Expected near 2*cores");
    }

    @Test
    void recommendPoolSize_appliesSafetyCap() {
        int cores = Runtime.getRuntime().availableProcessors();
        int maxReasonable = cores * 50;

        // huge wait/compute ratio => should cap
        int r = ThreadPoolTuner.recommendPoolSize(1_000_000, 1);
        assertTrue(r <= maxReasonable, "Expected capped thread count");
        assertTrue(r >= 1);
    }
}
