package edu.lu.concurrency.week2.day1.part3_coordination;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Demo15_JoinCorrectnessTest {

    @Test
    void demo15_runs_and_indicates_join_correctness() throws Exception {
        System.setProperty("demo.runs", "5");

        String out = TestIO.captureOut(() -> Demo15_JoinCorrectness.main(new String[0]));

        assertTrue(out.contains("[SUMMARY]") || out.contains("[TAKEAWAY]") || out.contains("TAKEAWAY"),
                "Expected summary/takeaway output indicating join-based coordination");
    }
}
