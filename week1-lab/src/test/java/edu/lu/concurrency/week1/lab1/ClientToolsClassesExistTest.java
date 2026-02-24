package edu.lu.concurrency.week1.lab1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientToolsClassesExistTest {

    @Test
    void toolClassesLoad() {
        assertDoesNotThrow(() -> Class.forName("edu.lu.concurrency.week1.lab1.LoadClient"));
        assertDoesNotThrow(() -> Class.forName("edu.lu.concurrency.week1.lab1.LoadTestClient"));
        assertDoesNotThrow(() -> Class.forName("edu.lu.concurrency.week1.lab1.EnhancedLoadClient"));

        assertDoesNotThrow(() -> Class.forName("edu.lu.concurrency.week1.lab1.ThreadPoolTuner"));
        assertDoesNotThrow(() -> Class.forName("edu.lu.concurrency.week1.lab1.IOProfiler"));
        assertDoesNotThrow(() -> Class.forName("edu.lu.concurrency.week1.lab1.AutoTuner"));
    }
}
