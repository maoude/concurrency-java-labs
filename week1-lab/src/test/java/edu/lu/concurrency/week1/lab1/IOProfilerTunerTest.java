package edu.lu.concurrency.week1.lab1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IOProfilerTunerTest {

    @Test
    void recommendPoolSize_validatesInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> IOProfiler.recommendPoolSize(0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> IOProfiler.recommendPoolSize(10, 0));
        assertThrows(IllegalArgumentException.class,
                () -> IOProfiler.recommendPoolSize(-1, 10));
    }

    @Test
    void recommendPoolSize_behavesLikeWaitComputeHeuristic() {
        int cores = Runtime.getRuntime().availableProcessors();

        int nearCores = IOProfiler.recommendPoolSize(0, 10);
        assertTrue(Math.abs(nearCores - cores) <= 1, "Expected near cores");

        int bigger = IOProfiler.recommendPoolSize(100, 10);
        assertTrue(bigger > nearCores, "Expected larger pool when wait dominates");
    }
}
