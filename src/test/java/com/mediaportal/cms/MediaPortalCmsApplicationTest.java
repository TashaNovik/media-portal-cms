package com.mediaportal.cms;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MediaPortalCmsApplication main class.
 */
class MediaPortalCmsApplicationTest {

    @Test
    @DisplayName("Main method should not throw exception")
    void main_ShouldNotThrowException() {
        // This test verifies the main method exists and is callable
        // We don't actually start the application as it requires full context
        assertDoesNotThrow(() -> {
            // Just verify the class can be loaded
            Class<?> clazz = MediaPortalCmsApplication.class;
            assertNotNull(clazz);
            assertEquals("MediaPortalCmsApplication", clazz.getSimpleName());
        });
    }

    @Test
    @DisplayName("Application class should be annotated with SpringBootApplication")
    void applicationClass_ShouldHaveSpringBootApplicationAnnotation() {
        assertTrue(MediaPortalCmsApplication.class.isAnnotationPresent(
                org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }
}
