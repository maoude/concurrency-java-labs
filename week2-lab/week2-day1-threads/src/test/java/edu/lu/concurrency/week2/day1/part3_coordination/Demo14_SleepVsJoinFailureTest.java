package edu.lu.concurrency.week2.day1.part3_coordination;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Demo14_SleepVsJoinFailureTest {

    @Test
    void demo14_runs_and_prints_summary() throws Exception {
        System.setProperty("demo.runs", "5");
        System.setProperty("demo.sleepMs", "0"); // make failure very likely + fast

        String out = TestIO.captureOut(() -> Demo14_SleepVsJoinFailure.main(new String[0]));

        assertTrue(out.contains("[SUMMARY]"), "Expected a [SUMMARY] line in output");
        assertTrue(out.contains("[TAKEAWAY]"), "Expected TAKEAWAY lines in output");
    }
}
