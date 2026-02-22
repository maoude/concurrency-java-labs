package edu.lu.concurrency.week2.day1.part3_coordination;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

final class TestIO {
    static String captureOut(ThrowingRunnable r) throws Exception {
        PrintStream old = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            System.setOut(ps);
            r.run();
        } finally {
            System.setOut(old);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    @FunctionalInterface
    interface ThrowingRunnable { void run() throws Exception; }

    private TestIO() {}
}
