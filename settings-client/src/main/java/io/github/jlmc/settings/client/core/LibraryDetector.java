package io.github.jlmc.settings.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LibraryDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryDetector.class);

    private LibraryDetector() {} // prevent instantiation

    /**
     * Checks if all the specified classes are present on the runtime classpath.
     * Uses the thread context ClassLoader for safety.
     *
     * @param classNames Fully qualified class names to check
     * @return true if all classes are present, false if any are missing
     */
    static boolean areClassesAvailable(String... classNames) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = LibraryDetector.class.getClassLoader();

        for (String className : classNames) {
            try {
                cl.loadClass(className);
            } catch (ClassNotFoundException e) {
                LOGGER.warn("Class {} not found on classpath.", className);
                return false;
            }
        }
        return true;
    }

    static boolean isNimbusAvailable() {
        return areClassesAvailable(
                "com.nimbusds.jwt.SignedJWT",
                "com.nimbusds.jose.JWSHeader",
                "com.nimbusds.jose.crypto.ECDSASigner"
        );
    }

    static boolean isResilience4jRetryAvailable() {
        return areClassesAvailable(
                "io.github.resilience4j.retry.Retry",
                "io.github.resilience4j.retry.RetryConfig"
        );
    }
}
