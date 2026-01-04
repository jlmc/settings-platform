package io.github.jlmc.settings.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jlmc.settings.client.auth.AuthCredentials;
import io.github.jlmc.settings.client.http.HttpConstants;
import io.github.jlmc.settings.client.http.HttpExecutionStrategy;
import io.github.jlmc.settings.client.json.JacksonJsonDeserializer;
import io.github.jlmc.settings.client.json.JsonDeserializer;
import io.github.jlmc.settings.client.redis.DistributedConfigProvider;
import io.github.jlmc.settings.client.resilience.Resilience4jRetryExecutor;
import io.github.jlmc.settings.client.resilience.ResilientHttpExecutionStrategy;
import io.github.jlmc.settings.client.resilience.RetryExecutor;
import io.github.jlmc.settings.client.token.AccessTokenProvider;
import io.github.jlmc.settings.client.token.BearerTokenStrategy;
import io.github.jlmc.settings.client.token.ClientCredentialsStrategy;
import io.github.jlmc.settings.client.token.PrivilegedCredentialsStrategy;
import io.github.jlmc.settings.client.token.TokenAcquisitionStrategy;
import io.github.jlmc.settings.client.token.TokenOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Builder {

    private static final Logger LOGGER = LoggerFactory.getLogger(Builder.class);

    private String apiBaseUrl;
    private Duration connectionTimeout = HttpConstants.DEFAULT_CONNECTION_TIMEOUT;
    private Duration requestTimeout = HttpConstants.DEFAULT_REQUEST_TIMEOUT;
    private String userAgent = "industries-settings-client-java";
    private JsonDeserializer jsonDeserializer = defaultJsonDeserializer();
    private RetryExecutor retryExecutor;
    private HttpExecutionStrategy httpExecutionStrategy;
    private AccessTokenProvider accessTokenProvider;
    private DistributedConfigProvider distributedConfigProvider;
    private boolean useRetryExecutor = false;

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
                        : lazyDefaultAccessTokenProvider(httpStrategy);

        return new IndustriesSettingsClient(
                apiBaseUrl,
                httpStrategy,
                tokenProvider,
                jsonDeserializer,
                requestTimeout,
                distributedConfigProvider
        );
    }


    private AccessTokenProvider lazyDefaultAccessTokenProvider(HttpExecutionStrategy httpStrategy) {
        // Check if Nimbus JOSE JWT classes are on the classpath
        boolean nimbusAvailable;
        try {
            Class.forName("com.nimbusds.jwt.SignedJWT");
            Class.forName("com.nimbusds.jose.JWSHeader");
            Class.forName("com.nimbusds.jose.crypto.ECDSASigner");
            nimbusAvailable = true;
            LOGGER.debug("Nimbus JOSE JWT library detected on classpath.");
        } catch (ClassNotFoundException e) {
            nimbusAvailable = false;
            LOGGER.warn("Nimbus JOSE JWT library not found on classpath. PrivilegedCredentialsStrategy will not be added.");
        }

        List<TokenAcquisitionStrategy<? extends AuthCredentials>> strategies = new ArrayList<>();
        strategies.add(new BearerTokenStrategy());
        strategies.add(new ClientCredentialsStrategy(httpStrategy, jsonDeserializer));

        if (nimbusAvailable) {
            strategies.add(new PrivilegedCredentialsStrategy(httpStrategy, jsonDeserializer));
            LOGGER.info("PrivilegedCredentialsStrategy added to TokenOrchestrator.");
        } else {
            LOGGER.info("TokenOrchestrator will be created without PrivilegedCredentialsStrategy.");
        }

        return new TokenOrchestrator(strategies);
    }

    private RetryExecutor retryExecutor() {
        if (!useRetryExecutor) {
            LOGGER.info("RetryExecutor is disabled via configuration.");
            return null;
        }

        if (retryExecutor != null) {
            LOGGER.debug("Returning cached RetryExecutor instance.");
            return retryExecutor;
        }

        // Check if Resilience4j classes are on the classpath
        boolean resilience4jAvailable;
        try {
            Class.forName("io.github.resilience4j.retry.Retry");
            Class.forName("io.github.resilience4j.retry.RetryConfig");
            resilience4jAvailable = true;
            LOGGER.debug("Resilience4j classes detected on classpath.");
        } catch (ClassNotFoundException e) {
            resilience4jAvailable = false;
            LOGGER.warn("Resilience4j classes not found on classpath. RetryExecutor will not be created.");
        }

        if (!resilience4jAvailable) {
            return null;
        }

        // Lazily initialize the default RetryExecutor
        retryExecutor = Resilience4jRetryExecutor.defaultRetryExecutor();
        LOGGER.info("Default Resilience4jRetryExecutor initialized.");
        return retryExecutor;
    }


    /* =========================
       Defaults
       ========================= */

    public static JsonDeserializer defaultJsonDeserializer() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        return new JacksonJsonDeserializer(objectMapper);
    }

}
