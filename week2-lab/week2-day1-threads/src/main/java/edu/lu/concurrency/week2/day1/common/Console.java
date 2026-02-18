package edu.lu.concurrency.week2.day1.common;

public final class Console {

    private static final boolean VERBOSE = Boolean.getBoolean("verbose");

    private Console() {}

    public static void info(String message) {
        System.out.println(message);
    }

    public static void verbose(String message) {
        if (VERBOSE) {
            System.out.println("[VERBOSE] " + message);
        }
    }
}
