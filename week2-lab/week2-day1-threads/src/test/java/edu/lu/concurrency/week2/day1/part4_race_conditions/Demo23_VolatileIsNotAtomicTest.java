package edu.lu.concurrency.week2.day1.part4_race_conditions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Demo23_VolatileIsNotAtomicTest {

    @Test
    void demo23_runs_and_mentions_volatile_not_atomic() throws Exception {
        String out = TestIO.captureStdout(() -> Demo23_VolatileIsNotAtomic.main(new String[0]));
        assertTrue(out.contains("volatile != atomic"));
        assertTrue(out.contains("[RESULT] expected="));
        assertTrue(out.contains("TAKEAWAY"));
    }
}