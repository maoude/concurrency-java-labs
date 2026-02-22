package edu.lu.concurrency.week2.day1.part4_race_conditions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Demo20_JoinDoesNotFixRaceTest {

    @Test
    void demo20_runs_and_mentions_join_not_fix() throws Exception {
        String out = TestIO.captureStdout(() -> Demo20_JoinDoesNotFixRace.main(new String[0]));
        assertTrue(out.contains("join() does NOT fix"));
        assertTrue(out.contains("[RESULT]"));
        assertTrue(out.contains("TAKEAWAY"));
    }
}