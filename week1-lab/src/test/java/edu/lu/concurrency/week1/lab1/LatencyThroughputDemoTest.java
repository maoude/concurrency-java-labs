package edu.lu.concurrency.week1.lab1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LatencyThroughputDemoTest {
    @Test void runsWithoutThrowing() {
        assertDoesNotThrow(() -> LatencyThroughputDemo.main(new String[0]));
    }
}