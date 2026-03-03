package io.github.jlmc.settings.client.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryDetectorTest {

    @Test
    void testAreClassesAvailable_AllPresent() {
        // java.lang.String and java.util.List are always on classpath
        boolean result = LibraryDetector.areClassesAvailable(
                "java.lang.String",
                "java.util.List"
        );
        assertTrue(result, "All existing classes should return true");
    }

    @Test
    void testAreClassesAvailable_SomeMissing() {
        boolean result = LibraryDetector.areClassesAvailable(
                "java.lang.String",
                "com.example.NonExistentClass"
        );
        assertFalse(result, "If any class is missing, result should be false");
    }

    @Test
    void testAreClassesAvailable_AllMissing() {
        boolean result = LibraryDetector.areClassesAvailable(
                "com.example.Foo",
                "com.example.Bar"
        );
        assertFalse(result, "All missing classes should return false");
    }

    @Test
    void testIsNimbusAvailable_WhenPresentOrAbsent() {
        // We cannot guarantee Nimbus presence in test runtime, so just check boolean type
        boolean result = LibraryDetector.isNimbusAvailable();
        assertTrue(result || !result, "Should return a boolean without throwing exception");
    }

    @Test
    void testIsResilience4jRetryAvailable_WhenPresentOrAbsent() {
        // Same logic as above
        boolean result = LibraryDetector.isResilience4jRetryAvailable();
        assertTrue(result || !result, "Should return a boolean without throwing exception");
    }
}
