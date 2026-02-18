package edu.lu.concurrency.week2.day1.common;

import java.time.Instant;

public final class Timestamp {

    private Timestamp() {}

    public static String now() {
        return Instant.now().toString();
    }
}
