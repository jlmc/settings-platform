package io.github.jlmc.settings.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jlmc.settings.client.http.HttpConstants;
import io.github.jlmc.settings.client.http.HttpExecutionStrategy;
import io.github.jlmc.settings.client.json.JacksonJsonDeserializer;
import io.github.jlmc.settings.client.json.JsonDeserializer;
import io.github.jlmc.settings.client.redis.DistributedConfigProvider;
import io.github.jlmc.settings.client.resilience.ResilientHttpExecutionStrategy;
import io.github.jlmc.settings.client.resilience.RetryExecutor;
import io.github.jlmc.settings.client.token.AccessTokenProvider;
import io.github.jlmc.settings.client.token.BearerTokenStrategy;
import io.github.jlmc.settings.client.token.ClientCredentialsStrategy;
import io.github.jlmc.settings.client.token.PrivilegedCredentialsStrategy;
import io.github.jlmc.settings.client.token.TokenOrchestrator;

import java.time.Duration;
import java.util.List;

public class Builder {

    private String apiBaseUrl;
    private Duration connectionTimeout = HttpConstants.DEFAULT_CONNECTION_TIMEOUT;
    private Duration requestTimeout = HttpConstants.DEFAULT_REQUEST_TIMEOUT;
    private String userAgent = "industries-settings-client-java";
    private JsonDeserializer jsonDeserializer = defaultJsonDeserializer();
    private RetryExecutor retryExecutor; // TODO: initialize with defaultRetryExecutor()
    private HttpExecutionStrategy httpExecutionStrategy;
    private AccessTokenProvider accessTokenProvider;
    private DistributedConfigProvider distributedConfigProvider;
    private boolean useRetryExecutor = false; // TODO: change to true once defaultRetryExecutor is implemented

    public Builder apiBaseUrl(String value) {
        this.apiBaseUrl = value;
        return this;
    }

    public Builder connectionTimeout(Duration value) {
        this.connectionTimeout = value;
        return this;
    }

    public Builder requestTimeout(Duration value) {
        this.requestTimeout = value;
        return this;
    }

    public Builder userAgent(String value) {
        this.userAgent = value;
        return this;
    }

    public Builder jsonDeserializer(JsonDeserializer value) {
        this.jsonDeserializer = value;
        return this;
    }

    public Builder retryExecutor(RetryExecutor value) {
        this.retryExecutor = value;
        return this;
    }

    public Builder useRetryExecutor(boolean value) {
        this.useRetryExecutor = value;
        return this;
    }

    public Builder redisExecutionStrategy(DistributedConfigProvider value) {
        this.distributedConfigProvider = value;
        return this;
    }

    public Builder httpExecutionStrategy(HttpExecutionStrategy value) {
        this.httpExecutionStrategy = value;
        return this;
    }

    public Builder accessTokenProvider(AccessTokenProvider value) {
        this.accessTokenProvider = value;
        return this;
    }


    public IndustriesSettingsClient build() {
        if (apiBaseUrl == null) {
            throw new IllegalStateException("apiBaseUrl is required");
        }

        HttpExecutionStrategy httpStrategy =
                httpExecutionStrategy != null
                        ? httpExecutionStrategy
                        : ResilientHttpExecutionStrategy.createDefault(
                        connectionTimeout,
                        requestTimeout,
                        userAgent,
                        retryExecutor()
                );

        AccessTokenProvider tokenProvider =
                accessTokenProvider != null
                        ? accessTokenProvider
                        : new TokenOrchestrator(
                            List.of(
                                new BearerTokenStrategy(),
                                new ClientCredentialsStrategy(httpStrategy, jsonDeserializer),
                                new PrivilegedCredentialsStrategy(httpStrategy, jsonDeserializer)
                        )
                );

        return new IndustriesSettingsClient(
                apiBaseUrl,
                httpStrategy,
                tokenProvider,
                jsonDeserializer,
                requestTimeout,
                distributedConfigProvider
        );
    }

    private RetryExecutor retryExecutor() {
        return null;
        /*
        if (!useRetryExecutor) {
            return null;
        }
        return retryExecutor != null ? retryExecutor : defaultRetryExecutor();

         */
    }

    /* =========================
       Defaults
       ========================= */

    public static JsonDeserializer defaultJsonDeserializer() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        return new JacksonJsonDeserializer(objectMapper);
    }

    /*
    public static RetryExecutor defaultRetryExecutor() {
        Set<Integer> retriableStatuses =
                Set.of(
                                HttpStatusCode.REQUEST_TIMEOUT,
                                HttpStatusCode.INTERNAL_SERVER_ERROR,
                                HttpStatusCode.BAD_GATEWAY,
                                HttpStatusCode.SERVICE_UNAVAILABLE,
                                HttpStatusCode.GATEWAY_TIMEOUT
                        ).stream()
                        .map(HttpStatusCode::getCode)
                        .collect(Collectors.toSet());

        RetryConfig<Object> config =
                RetryConfig.custom()
                        .maxAttempts(HttpConstants.DEFAULT_MAX_RETRIES)
                        .intervalFunction(IntervalFunction.ofExponentialBackoff(200L, 2.0))
                        .retryOnException(ex -> {
                            if (ex instanceof SettingsClientException sce) {
                                Throwable cause = sce.getCause();
                                return cause instanceof HttpTimeoutException
                                        || cause instanceof SocketTimeoutException
                                        || cause instanceof ConnectException
                                        || cause instanceof SocketException
                                        || cause instanceof EOFException;
                            }
                            if (ex instanceof SettingsClientHttpException httpEx) {
                                return retriableStatuses.contains(httpEx.getStatusCode());
                            }
                            return false;
                        })
                        .build();

        Retry retry = Authenticator.Retry.of("industriesSettingsClientRetry", config);
        return new Resilience4jRetryExecutor(retry);
    }
     */

    /* =========================
       Redis Builder
       ========================= */
}
