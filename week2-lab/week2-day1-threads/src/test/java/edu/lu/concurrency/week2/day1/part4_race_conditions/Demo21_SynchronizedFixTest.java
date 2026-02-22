package edu.lu.concurrency.week2.day1.part4_race_conditions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Demo21_SynchronizedFixTest {

    @Test
    void demo21_runs_and_prints_result() throws Exception {
        String out = TestIO.captureStdout(() -> Demo21_SynchronizedFix.main(new String[0]));
        assertTrue(out.contains("synchronized Fix"));
        assertTrue(out.contains("[RESULT] expected="));
    }
}