package org.example.team6backend;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SanityCheckTest {

    @Test
    void testJUnitWorks() {
        assertTrue(true);
        assertEquals(1, 1);
    }

    @Test
    void testStringOperations() {
        String message = "CI/CD pipeline works";
        assertNotNull(message);
        assertTrue(message.contains("works"));
    }
}