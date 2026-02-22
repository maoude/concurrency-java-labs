package edu.lu.concurrency.week2.day1.part4_race_conditions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Demo22_AtomicIntegerFixTest {

    @Test
    void demo22_runs_and_prints_result() throws Exception {
        String out = TestIO.captureStdout(() -> Demo22_AtomicIntegerFix.main(new String[0]));
        assertTrue(out.contains("AtomicInteger Fix"));
        assertTrue(out.contains("[RESULT] expected="));
    }
}