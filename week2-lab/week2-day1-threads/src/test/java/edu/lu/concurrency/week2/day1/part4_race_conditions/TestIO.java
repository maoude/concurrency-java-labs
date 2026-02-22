package edu.lu.concurrency.week2.day1.part4_race_conditions;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class TestIO {

    public static String captureStdout(ThrowingRunnable r) throws Exception {
        PrintStream old = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8);
        try {
            System.setOut(ps);
            r.run();
        } finally {
            System.setOut(old);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    private TestIO() {}
}