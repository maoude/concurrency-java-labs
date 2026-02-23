package edu.lu.concurrency.week1.lab1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AmdahlCalculatorTest {
    @Test void runsAndPrintsSomething() throws Exception {
        String out = TestConsole.captureStdout(() -> AmdahlCalculator.main(new String[0]));
        assertFalse(out.trim().isEmpty());
    }
}