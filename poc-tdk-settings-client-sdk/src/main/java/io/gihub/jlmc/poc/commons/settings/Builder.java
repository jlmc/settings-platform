package io.gihub.jlmc.poc.commons.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gihub.jlmc.poc.commons.settings.http.HttpConstants;
import io.gihub.jlmc.poc.commons.settings.http.HttpExecutionStrategy;
import io.gihub.jlmc.poc.commons.settings.json.JacksonJsonDeserializer;
import io.gihub.jlmc.poc.commons.settings.json.JsonDeserializer;
import io.gihub.jlmc.poc.commons.settings.redis.RedisExecutionStrategy;
import io.gihub.jlmc.poc.commons.settings.resilience.ResilientHttpExecutionStrategy;
import io.gihub.jlmc.poc.commons.settings.resilience.RetryExecutor;
import io.gihub.jlmc.poc.commons.settings.token.AccessTokenProvider;
import io.gihub.jlmc.poc.commons.settings.token.BearerTokenStrategy;
import io.gihub.jlmc.poc.commons.settings.token.ClientCredentialsStrategy;
import io.gihub.jlmc.poc.commons.settings.token.PrivilegedCredentialsStrategy;
import io.gihub.jlmc.poc.commons.settings.token.TokenOrchestrator;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public class Builder {

    private String apiBaseUrl;
    private Duration connectionTimeout = HttpConstants.DEFAULT_CONNECTION_TIMEOUT;
    private Duration requestTimeout = HttpConstants.DEFAULT_REQUEST_TIMEOUT;
    private String userAgent = "industries-settings-client-java";
    private JsonDeserializer jsonDeserializer = defaultJsonDeserializer();
    private RetryExecutor retryExecutor; // TODO: initialize with defaultRetryExecutor()
    private HttpExecutionStrategy httpExecutionStrategy;
    private AccessTokenProvider accessTokenProvider;
    private RedisExecutionStrategy redisExecutionStrategy; // TODO: initialize with defaultRedisExecutionStrategy()
    private RedisBuilder redisBuilder;
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

    public Builder httpExecutionStrategy(HttpExecutionStrategy value) {
        this.httpExecutionStrategy = value;
        return this;
    }

    public Builder accessTokenProvider(AccessTokenProvider value) {
        this.accessTokenProvider = value;
        return this;
    }

    public Builder redis(Function<RedisBuilder, RedisBuilder> block) {
        this.redisBuilder = block.apply(new RedisBuilder());
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

        RedisExecutionStrategy redisStrategy =
                redisExecutionStrategy != null
                        ? redisExecutionStrategy
                        : redisBuilder != null
                        ? redisBuilder.build(jsonDeserializer)
                        : null;


        /*
           private IndustriesSettingsClient(
            String apiBaseUrl,
            HttpExecutionStrategy httpExecutionStrategy,
            AccessTokenProvider accessTokenProvider,
            JsonDeserializer jsonDeserializer,
            Duration requestTimeout,
            RedisExecutionStrategy redisExecutionStrategy
    )
         */

        return new IndustriesSettingsClient(
                apiBaseUrl,
                httpStrategy,
                tokenProvider,
                jsonDeserializer,
                requestTimeout,
                redisStrategy
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

    public static class RedisBuilder {

        private String redisAddress = "redis://127.0.0.1:6379/0";
        private String redisPassword;
        private boolean cluster;
        private String namespace = "industries_settings";
        private Function<ConfigurationRequest, String> accountIdProvider;

        public RedisBuilder address(String value) {
            this.redisAddress = value;
            return this;
        }

        public RedisBuilder password(String value) {
            this.redisPassword = value;
            return this;
        }

        public RedisBuilder cluster(boolean value) {
            this.cluster = value;
            return this;
        }

        public RedisBuilder namespace(String value) {
            this.namespace = value;
            return this;
        }

        public RedisBuilder accountIdProvider(Function<ConfigurationRequest, String> value) {
            this.accountIdProvider = value;
            return this;
        }

        public RedisExecutionStrategy build(JsonDeserializer jsonDeserializer) {
            if (accountIdProvider == null) {
                throw new IllegalStateException(
                        "Redis accountIdProvider is required when Redis is enabled"
                );
            }

            return RedisExecutionStrategy.createDefault(
                    jsonDeserializer,
                    redisAddress,
                    redisPassword,
                    cluster,
                    namespace,
                    accountIdProvider
            );
        }
    }
}
