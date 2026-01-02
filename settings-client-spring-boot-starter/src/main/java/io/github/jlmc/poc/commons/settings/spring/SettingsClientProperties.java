package io.github.jlmc.poc.commons.settings.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "industries.settings.client")
public class SettingsClientProperties {

    /**
     * Base URL for the Industries Settings API.
     */
    private String apiBaseUrl;

    /**
     * Connection timeout for HTTP requests.
     */
    private Duration connectionTimeout = Duration.ofSeconds(5);

    /**
     * Request timeout for HTTP requests.
     */
    private Duration requestTimeout = Duration.ofSeconds(10);

    /**
     * User agent string to use for HTTP requests.
     */
    private String userAgent = "industries-settings-client-java";

    /**
     * Whether to use a retry executor for resilient HTTP execution.
     */
    private boolean useRetryExecutor = false;

    /**
     * Whether Redis distribution is enabled.
     */
    private boolean redisEnabled = true;

    /**
     * Redis namespace for settings keys.
     */
    private String namespace = "settings";

    /**
     * Time-to-live for Redis L1 cache.
     */
    private Duration redisL1Ttl = Duration.ofHours(10);

    /**
     * Maximum size for Redis L1 cache.
     */
    private long redisL1MaxSize = 1000;

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean isUseRetryExecutor() {
        return useRetryExecutor;
    }

    public void setUseRetryExecutor(boolean useRetryExecutor) {
        this.useRetryExecutor = useRetryExecutor;
    }

    public boolean isRedisEnabled() {
        return redisEnabled;
    }

    public void setRedisEnabled(boolean redisEnabled) {
        this.redisEnabled = redisEnabled;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Duration getRedisL1Ttl() {
        return redisL1Ttl;
    }

    public void setRedisL1Ttl(Duration redisL1Ttl) {
        this.redisL1Ttl = redisL1Ttl;
    }

    public long getRedisL1MaxSize() {
        return redisL1MaxSize;
    }

    public void setRedisL1MaxSize(long redisL1MaxSize) {
        this.redisL1MaxSize = redisL1MaxSize;
    }
}
