package io.github.jlmc.settings.client.adapters.resilience;

import io.github.jlmc.settings.client.adapters.http.ClientHttpExecutor;
import io.github.jlmc.settings.client.adapters.http.ClientHttpRequest;
import io.github.jlmc.settings.client.adapters.http.DefaultClientHttpExecutor;
import io.github.jlmc.settings.client.adapters.http.HttpConstants;
import io.github.jlmc.settings.client.ports.out.HttpExecutionStrategy;
import io.github.jlmc.settings.client.ports.out.RetryExecutor;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

public class ResilientHttpExecutionStrategy implements HttpExecutionStrategy {

    private final ClientHttpExecutor clientHttpExecutor;
    private final RetryExecutor retryExecutor;

    public ResilientHttpExecutionStrategy(ClientHttpExecutor clientHttpExecutor) {
        this(clientHttpExecutor, null);
    }

    public ResilientHttpExecutionStrategy(ClientHttpExecutor clientHttpExecutor, RetryExecutor retryExecutor) {
        this.clientHttpExecutor = Objects.requireNonNull(clientHttpExecutor, "clientHttpExecutor must not be null");
        this.retryExecutor = retryExecutor;
    }

    /**
     * Factory method to create a default instance with optional parameters.
     */
    public static ResilientHttpExecutionStrategy createDefault() {
        return createDefault(
                HttpConstants.DEFAULT_CONNECTION_TIMEOUT,
                HttpConstants.DEFAULT_REQUEST_TIMEOUT,
                null,
                null
        );
    }

    public static ResilientHttpExecutionStrategy createDefault(Duration connectionTimeout,
                                                               Duration defaultRequestTimeout,
                                                               String userAgent,
                                                               RetryExecutor retryExecutor) {
        DefaultClientHttpExecutor executor = new DefaultClientHttpExecutor(
                connectionTimeout,
                defaultRequestTimeout,
                userAgent,
                null
        );
        return new ResilientHttpExecutionStrategy(executor, retryExecutor);
    }

    @Override
    public String execute(ClientHttpRequest request) {
        Supplier<String> supplier = () -> performRequest(request);
        return retryExecutor != null ? retryExecutor.execute(supplier) : supplier.get();
    }

    private String performRequest(ClientHttpRequest request) {
        String body = clientHttpExecutor.send(request).body();
        return body != null ? body : "";
    }

    @Override
    public void close() throws Exception {
        if (clientHttpExecutor != null) {
            this.clientHttpExecutor.close();
        }
    }
}
