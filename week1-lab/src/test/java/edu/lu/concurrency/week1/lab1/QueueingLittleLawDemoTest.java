package edu.lu.concurrency.week1.lab1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QueueingLittleLawDemoTest {
    @Test void runsWithoutThrowing() {
        assertDoesNotThrow(() -> QueueingLittleLawDemo.main(new String[0]));
    }
}