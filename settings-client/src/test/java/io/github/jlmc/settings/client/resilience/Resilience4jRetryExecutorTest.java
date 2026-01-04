package io.github.jlmc.settings.client.resilience;

import io.github.jlmc.settings.client.exceptions.SettingsClientException;
import io.github.jlmc.settings.client.exceptions.SettingsClientHttpException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Resilience4jRetryExecutorTest {

    private Supplier<String> mockSupplier;
    private Resilience4jRetryExecutor victim;

    @BeforeEach
    void setUp() {
        //noinspection unchecked
        mockSupplier = mock(Supplier.class);

        // Configure Resilience4j Retry for 3 attempts and zero delay (for quick testing)
        RetryConfig retryConfig = RetryConfig.<String>custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(0))
                .ignoreExceptions(SettingsClientException.class)
                .build();

        String testRetryName = "testRetry";
        Retry retryInstance = Retry.of(testRetryName, retryConfig);

        victim = new Resilience4jRetryExecutor(retryInstance);
    }

    @Test
    void shouldExecuteSupplierSuccessfullyOnFirstAttempt() {
        // GIVEN: Supplier returns success
        String expectedResult = "TokenA";
        when(mockSupplier.get()).thenReturn(expectedResult);

        // WHEN: We run the executor
        String result = victim.execute(mockSupplier);

        // THEN: The result must be as expected and the Supplier must be called only once
        assertEquals(expectedResult, result);
        verify(mockSupplier, times(1)).get();
    }

    @Test
    void shouldSucceedOnThirdAttemptAfterTwoFailures() {
        // GIVEN: Supplier fails twice and succeeds the third time (limit of 3 attempts)
        String expectedResult = "TokenB";

        when(mockSupplier.get())
                .thenThrow(new UncheckedIOException(new ConnectException("Retryable failure 1")))
                .thenThrow(new UncheckedIOException(new SocketTimeoutException("Retryable failure 2")))
                .thenReturn(expectedResult);

        // WHEN: We run the executor
        String result = victim.execute(mockSupplier);

        // THEN: The result must be success, and the Supplier must be called 3 times
        assertEquals(expectedResult, result);
        verify(mockSupplier, times(3)).get();
    }

    @Test
    void shouldRethrowExistingSettingsClientExceptionAfterMaxAttempts() {
        // GIVEN: Provider fails 1 time with a SettingsClientException
        SettingsClientException existingException = new SettingsClientException("Settings Client Exception");
        when(mockSupplier.get()).thenThrow(existingException);

        // WHEN and THEN: Executing should throw the *same* SettingsClientException
        SettingsClientException exception = assertThrows(
                SettingsClientException.class,
                () -> victim.execute(mockSupplier)
        );

        verify(mockSupplier, times(1)).get();
        assertEquals(existingException, exception);
    }

    @Test
    void shouldCreateExceptionWithStatusBodyAndMessage() {
        // GIVEN: Provider fails 3 times with a SettingsClientHttpException
        SettingsClientHttpException ex = new SettingsClientHttpException(429, "Too Many Requests");
        when(mockSupplier.get()).thenThrow(ex);

        // WHEN and THEN: Executing should throw the *same* exception
        SettingsClientHttpException exception = assertThrows(
                SettingsClientHttpException.class,
                () -> victim.execute(mockSupplier)
        );

        verify(mockSupplier, times(3)).get();
        assertEquals(ex, exception);
    }
}
