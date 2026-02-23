package edu.lu.concurrency.week1.lab1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServerClassesExistTest {
    @Test void classesLoad() {
        assertDoesNotThrow(() -> Class.forName("edu.lu.concurrency.week1.lab1.SingleThreadedServer"));
        assertDoesNotThrow(() -> Class.forName("edu.lu.concurrency.week1.lab1.MultiThreadedServer"));
        assertDoesNotThrow(() -> Class.forName("edu.lu.concurrency.week1.lab1.ImprovedMultiThreadedServer"));
        assertDoesNotThrow(() -> Class.forName("edu.lu.concurrency.week1.lab1.CPUBoundServer"));
        assertDoesNotThrow(() -> Class.forName("edu.lu.concurrency.week1.lab1.AsyncServer"));
        assertDoesNotThrow(() -> Class.forName("edu.lu.concurrency.week1.lab1.SingleThreadEventLoop"));
    }
}