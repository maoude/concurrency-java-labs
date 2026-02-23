package edu.lu.concurrency.week1.lab1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SmokeTest {
    @Test void amdahlRuns() {
        assertDoesNotThrow(() -> AmdahlCalculator.main(new String[0]));
    }
}