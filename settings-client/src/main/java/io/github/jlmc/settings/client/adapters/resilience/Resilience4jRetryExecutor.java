package io.github.jlmc.settings.client.adapters.resilience;

import io.github.jlmc.settings.client.core.exceptions.SettingsClientException;
import io.github.jlmc.settings.client.core.exceptions.SettingsClientHttpException;
import io.github.jlmc.settings.client.ports.out.RetryExecutor;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.Set;
import java.util.function.Supplier;


/// Default implementation of [RetryExecutor] backed by the Resilience4j library.
///
/// This executor decorates a [Supplier] with a configured [Retry] policy,
/// allowing transient failures to be retried automatically according to
/// the retry configuration (e.g., number of attempts, wait duration, or backoff strategy).
public class Resilience4jRetryExecutor implements RetryExecutor {

    private static final Logger logger = LoggerFactory.getLogger(Resilience4jRetryExecutor.class);

    public static final Set<Integer> RETRIABLE_HTTP_STATUSES = Set.of(
            408, // REQUEST_TIMEOUT
            500, // INTERNAL_SERVER_ERROR
            502, // BAD_GATEWAY
            503, // SERVICE_UNAVAILABLE
            504  // GATEWAY_TIMEOUT
    );
    public static final int DEFAULT_MAX_RETRIES = 3;

    private final Retry retry;

    public Resilience4jRetryExecutor(Retry retry) {
        this.retry = retry;

        int maxAttempts = retry.getRetryConfig().getMaxAttempts();

        retry.getEventPublisher()
                .onRetry(event -> {
                    Throwable cause = event.getLastThrowable();
                    String wait;
                    try {
                        wait = String.valueOf(event.getWaitInterval());
                    } catch (Exception e) {
                        wait = "n/a";
                    }

                    logger.warn(
                            "Retry '{}' attempt {} of {} after failure: {}: {}. Next retry in {}",
                            retry.getName(),
                            event.getNumberOfRetryAttempts(),
                            maxAttempts,
                            cause != null ? cause.getClass().getSimpleName() : "UnknownException",
                            cause != null && cause.getMessage() != null
                                    ? truncate(cause.getMessage(), 100)
                                    : "",
                            wait
                    );
                })
                .onError(event -> {
                    Throwable cause = event.getLastThrowable();
                    logger.warn(
                            "Retry '{}' exhausted after {} attempts. Last error: {}: {}",
                            retry.getName(),
                            event.getNumberOfRetryAttempts(),
                            cause != null ? cause.getClass().getName() : "UnknownException",
                            cause != null && cause.getMessage() != null
                                    ? truncate(cause.getMessage(), 100)
                                    : ""
                    );
                })
                .onSuccess(event -> {
                    int retries = event.getNumberOfRetryAttempts();
                    int totalAttempts = retries + 1;
                    logger.info(
                            "Retry '{}' succeeded after {} attempt(s) ({} retries)",
                            retry.getName(),
                            totalAttempts,
                            retries
                    );
                });
    }

    @Override
    public <T> T execute(Supplier<T> supplier) {
        Supplier<T> decorated = Retry.decorateSupplier(retry, supplier);
        return decorated.get();
    }

    private static String truncate(String value, int maxLength) {
        return value.length() <= maxLength
                ? value
                : value.substring(0, maxLength);
    }


    public static RetryExecutor defaultRetryExecutor() {
        return defaultRetryExecutor(
                DEFAULT_MAX_RETRIES,
                RETRIABLE_HTTP_STATUSES
        );
    }

    public static RetryExecutor defaultRetryExecutor(int maxRetries) {
        return defaultRetryExecutor(
                maxRetries,
                RETRIABLE_HTTP_STATUSES
        );
    }

    public static RetryExecutor defaultRetryExecutor(
            int maxRetries,
            Set<Integer> retriableHttpStatuses
    ) {

        RetryConfig config =
                RetryConfig.custom()
                        .maxAttempts(maxRetries)
                        .intervalFunction(
                                IntervalFunction.ofExponentialBackoff(200L, 2.0)
                        )
                        .retryOnException(exception -> {

                            if (exception instanceof SettingsClientException sce) {
                                Throwable cause = sce.getCause();
                                return cause instanceof HttpTimeoutException
                                        || cause instanceof SocketTimeoutException
                                        || cause instanceof ConnectException
                                        || cause instanceof SocketException
                                        || cause instanceof EOFException;
                            }

                            if (exception instanceof SettingsClientHttpException httpException) {
                                return retriableHttpStatuses.contains(
                                        httpException.getStatusCode()
                                );
                            }

                            return false;
                        })
                        .build();

        Retry retry =
                Retry.of("industriesSettingsClientRetry", config);

        return new Resilience4jRetryExecutor(retry);
    }
}
