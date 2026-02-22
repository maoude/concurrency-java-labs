package edu.lu.concurrency.week2.day1.part3_coordination;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Demo16_SleepDoesNotGuaranteeOrderTest {

    @Test
    void demo16_runs_and_prints_summary() throws Exception {
        System.setProperty("demo.runs", "10");
        System.setProperty("demo.sleepMs", "0");

        String out = TestIO.captureOut(() -> Demo16_SleepDoesNotGuaranteeOrder.main(new String[0]));

        assertTrue(out.contains("[SUMMARY]"), "Expected a [SUMMARY] line in output");
        assertTrue(out.contains("[TAKEAWAY]"), "Expected TAKEAWAY lines in output");
    }
}
