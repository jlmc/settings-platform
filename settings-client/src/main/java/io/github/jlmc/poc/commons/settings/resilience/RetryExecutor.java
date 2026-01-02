package io.github.jlmc.poc.commons.settings.resilience;

import io.github.jlmc.poc.commons.settings.exceptions.SettingsClientException;

import java.util.function.Supplier;

/**
 * Interface for executing an action with an optional retry policy applied.
 * This decouples the concrete retry library (e.g., Resilience4j) from the business logic.
 */
public interface RetryExecutor {

    /**
     * Executes the provided Supplier, applying the configured retry policy.
     *
     * @param supplier The function (Supplier) to be executed.
     * @param <T> The type of the result returned by the Supplier.
     * @return The result of the execution.
     */
    <T> T execute(Supplier<T> supplier) throws SettingsClientException;
}
