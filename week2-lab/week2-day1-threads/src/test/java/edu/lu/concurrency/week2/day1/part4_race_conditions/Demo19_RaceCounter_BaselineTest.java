package edu.lu.concurrency.week2.day1.part4_race_conditions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Demo19_RaceCounter_BaselineTest {

    @Test
    void demo19_runs_and_prints_expected_and_actual() throws Exception {
        String out = TestIO.captureStdout(() -> Demo19_RaceCounter_Baseline.main(new String[0]));
        assertTrue(out.contains("Baseline Race"));
        assertTrue(out.contains("expected="));
        assertTrue(out.contains("actual="));
        assertTrue(out.contains("TAKEAWAY"));
    }
}