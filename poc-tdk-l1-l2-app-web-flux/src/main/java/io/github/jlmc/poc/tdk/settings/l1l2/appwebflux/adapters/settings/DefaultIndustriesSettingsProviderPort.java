package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.adapters.settings;

import io.gihub.jlmc.poc.commons.settings.ConfigurationRequest;
import io.gihub.jlmc.poc.commons.settings.IndustriesSettingsClient;
import io.gihub.jlmc.poc.commons.settings.exceptions.SettingsClientException;
import io.gihub.jlmc.poc.commons.settings.exceptions.SettingsClientHttpException;
import io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.ports.IndustriesSettingsProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.io.EOFException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

/**
 * Default WebFlux adapter for accessing Industries Settings.
 *
 * <p>
 * This adapter bridges a blocking SDK ({@link IndustriesSettingsClient})
 * into a reactive WebFlux pipeline.
 *
 * <p>
 * Key guarantees:
 * <ul>
 *   <li>Blocking calls are isolated from the event-loop using boundedElastic</li>
 *   <li>Transient failures are retried using reactive retryWhen</li>
 *   <li>Errors are never swallowed and are always propagated downstream</li>
 * </ul>
 */
public class DefaultIndustriesSettingsProviderPort implements IndustriesSettingsProviderPort {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DefaultIndustriesSettingsProviderPort.class);

    private final IndustriesSettingsClient industriesSettingsClient;

    public DefaultIndustriesSettingsProviderPort(IndustriesSettingsClient industriesSettingsClient) {
        this.industriesSettingsClient = industriesSettingsClient;
    }

    /**
     * Retrieves settings in a non-blocking way using WebFlux.
     *
     * <p>
     * {@link Mono#fromCallable} is intentionally used to wrap the blocking SDK call.
     * This ensures that:
     * <ul>
     *   <li>The execution is deferred until subscription time</li>
     *   <li>Any exception thrown by the blocking call is automatically captured</li>
     *   <li>Exceptions are propagated as an {@code onError} signal</li>
     * </ul>
     *
     * <p>
     * Because errors are propagated as {@code onError}, operators such as
     * {@code retryWhen} and {@code doOnError} work correctly.
     *
     * <p>
     * If all retry attempts are exhausted, the last error is propagated to the caller
     * without being swallowed or transformed.
     */
    @Override
    public <T> Mono<T> getSettings(ConfigurationRequest configurationRequest, Class<T> responseType) {

        return Mono.fromCallable(() -> {
                    LOGGER.debug(
                            "Fetching settings [service={}, objectType={}, objectId={}]",
                            configurationRequest.service(),
                            configurationRequest.objectType(),
                            configurationRequest.objectId()
                    );

                    // Blocking SDK call.
                    // Any exception thrown here will be captured by fromCallable
                    // and propagated downstream as onError.
                    return industriesSettingsClient
                            .getConfiguration(configurationRequest, responseType);
                })

                // Ensures the blocking call does NOT run on the Netty event-loop
                .subscribeOn(Schedulers.boundedElastic())

                // Retry is applied only for transient, retryable failures
                .retryWhen(retrySpec())

                // Successful retrieval
                .doOnSuccess(result ->
                        LOGGER.debug(
                                "Settings retrieved successfully [service={}, objectType={}]",
                                configurationRequest.service(),
                                configurationRequest.objectType()
                        )
                )

                // After retries are exhausted, the error is logged and propagated downstream
                .doOnError(error ->
                        LOGGER.error(
                                "Failed to retrieve settings after retries [service={}, objectType={}, objectId={}]",
                                configurationRequest.service(),
                                configurationRequest.objectType(),
                                configurationRequest.objectId(),
                                error
                        )
                );
    }

    /**
     * Defines retry configuration using exponential backoff.
     *
     * <p>
     * Only exceptions classified as retryable by {@link #isRetryable(Throwable)}
     * will trigger a retry.
     */
    private Retry retrySpec() {
        return Retry
                .backoff(3, Duration.ofMillis(200))
                .maxBackoff(Duration.ofSeconds(2))
                .jitter(0.3)
                .filter(this::isRetryable)
                .doBeforeRetry(retrySignal ->
                        LOGGER.warn(
                                "Retrying settings retrieval [attempt={}, cause={}]",
                                retrySignal.totalRetries() + 1,
                                retrySignal.failure().toString()
                        )
                );
    }

    /**
     * Determines whether an exception represents a transient failure.
     *
     * <p>
     * Retryable failures include:
     * <ul>
     *   <li>HTTP 5xx server-side errors</li>
     *   <li>Network connectivity issues</li>
     *   <li>Socket and request timeouts</li>
     * </ul>
     *
     * <p>
     * Non-retryable failures (e.g. HTTP 4xx, validation errors, deserialization errors)
     * are propagated immediately without retry.
     */
    private boolean isRetryable(Throwable t) {

        // Retry only on server-side HTTP errors (5xx)
        if (t instanceof SettingsClientHttpException http) {
            return http.getStatusCode() >= 500;
        }

        // Unwrap SDK exception to inspect the root cause
        if (t instanceof SettingsClientException sce) {
            return isRetryable(sce.getCause());
        }

        // Retry on transient network and timeout failures
        return t instanceof ConnectException
                || t instanceof SocketTimeoutException
                || t instanceof HttpTimeoutException
                || t instanceof SocketException
                || t instanceof EOFException;
    }
}
