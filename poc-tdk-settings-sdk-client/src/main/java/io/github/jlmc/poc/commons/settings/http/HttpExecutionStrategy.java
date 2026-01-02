package io.github.jlmc.poc.commons.settings.http;

/**
 * Defines a strategy for executing HTTP requests.
 *
 * Implementations may add cross-cutting concerns such as retry,
 * logging, telemetry, or circuit breaking.
 *
 * This interface abstracts the underlying HTTP client and allows
 * reusable execution strategies across multiple domains.
 */
public interface HttpExecutionStrategy {

    /**
     * Executes an HTTP request and returns the response body as a string.
     *
     * @param request The HTTP request to execute.
     * @return The response body.
     * @throws RuntimeException If a transport or non-successful HTTP error occurs.
     */
    String execute(ClientHttpRequest request);
}
