package edu.lu.concurrency.week1.lab1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SQLiteConcurrencyDemoTest {
    @Test void runsWithoutThrowing() {
        assertDoesNotThrow(() -> SQLiteConcurrencyDemo.main(new String[0]));
    }
}