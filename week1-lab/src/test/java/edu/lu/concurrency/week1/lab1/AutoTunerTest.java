package edu.lu.concurrency.week1.lab1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutoTunerTest {

    @Test
    void findOptimalPoolSize_currentImplementationReturnsMinSize() {
        int result = AutoTuner.findOptimalPoolSize(
                5,   // min
                15,  // max
                5,   // step
                5,   // clients
                1    // requests per client
        );
        assertEquals(5, result,
                "With runExperiment() returning 0.0, best stays minSize");
    }
}
