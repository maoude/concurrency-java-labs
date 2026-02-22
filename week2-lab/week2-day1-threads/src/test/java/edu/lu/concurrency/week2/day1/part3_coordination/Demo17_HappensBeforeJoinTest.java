package edu.lu.concurrency.week2.day1.part3_coordination;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Demo17_HappensBeforeJoinTest {

    @Test
    void demo17_runs_and_mentions_happens_before_or_visibility() throws Exception {
        System.setProperty("demo.runs", "10");

        String out = TestIO.captureOut(() -> Demo17_HappensBeforeJoin.main(new String[0]));

        // flexible assertion so it survives minor wording changes
        assertTrue(
                out.toLowerCase().contains("happens") ||
                out.toLowerCase().contains("visible") ||
                out.contains("[TAKEAWAY]") ||
                out.contains("[SUMMARY]"),
                "Expected output to reference visibility / happens-before / takeaway/summary"
        );
    }
}
