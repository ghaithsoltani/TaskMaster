package com.taskmaster;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test that loads the FULL Spring context.
 *
 * Verifies:
 * - Application starts successfully
 * - All beans are wired correctly
 * - No configuration conflicts
 *
 * Slower than unit tests but catches wiring issues.
 */
@SpringBootTest
@ActiveProfiles("test")
class TaskMasterApplicationTests {

    @Test
    void contextLoads() {
        // If this test passes, the Spring context loaded successfully
        // This is surprisingly valuable - it catches missing beans,
        // circular dependencies, and config errors
    }
}