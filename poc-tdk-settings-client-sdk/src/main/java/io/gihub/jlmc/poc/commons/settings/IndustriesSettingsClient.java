package io.gihub.jlmc.poc.commons.settings;

import io.gihub.jlmc.poc.commons.settings.exceptions.SettingsClientException;
import io.gihub.jlmc.poc.commons.settings.http.ClientHttpRequest;
import io.gihub.jlmc.poc.commons.settings.http.HttpConstants;
import io.gihub.jlmc.poc.commons.settings.http.HttpExecutionStrategy;
import io.gihub.jlmc.poc.commons.settings.http.HttpMethod;
import io.gihub.jlmc.poc.commons.settings.json.JsonDeserializer;
import io.gihub.jlmc.poc.commons.settings.redis.RedisExecutionStrategy;
import io.gihub.jlmc.poc.commons.settings.token.AccessTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * IndustriesSettingsClient provides a type-safe client for interacting with the Industries Settings API.
 */
public class IndustriesSettingsClient {

    //public static final String SETTINGS_CONFIGURATIONS_PATH = "industries-settings/configurations";
    //public static final String SETTINGS_CONFIGURATIONS_PATH = "configurations/1/my-service/account";
    public static final String SETTINGS_CONFIGURATIONS_PATH = "configurations";
    private static final Logger logger = LoggerFactory.getLogger(IndustriesSettingsClient.class);
    private final String apiBaseUrl;
    private final HttpExecutionStrategy httpExecutionStrategy;
    private final AccessTokenProvider accessTokenProvider;
    private final JsonDeserializer jsonDeserializer;
    private final Duration requestTimeout;
    private final RedisExecutionStrategy redisExecutionStrategy;

    protected IndustriesSettingsClient(
            String apiBaseUrl,
            HttpExecutionStrategy httpExecutionStrategy,
            AccessTokenProvider accessTokenProvider,
            JsonDeserializer jsonDeserializer,
            Duration requestTimeout,
            RedisExecutionStrategy redisExecutionStrategy
    ) {
        this.apiBaseUrl = apiBaseUrl;
        this.httpExecutionStrategy = httpExecutionStrategy;
        this.accessTokenProvider = accessTokenProvider;
        this.jsonDeserializer = jsonDeserializer;
        this.requestTimeout = requestTimeout;
        this.redisExecutionStrategy = redisExecutionStrategy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public <T> T getConfiguration(ConfigurationRequest configurationRequest, Class<T> responseType) {
        logger.debug("Starting configuration request for objectType='{}'{}",
                configurationRequest.objectType(),
                configurationRequest.objectId() != null ? ", objectId='" + configurationRequest.objectId() + "'" : "");

        T result = null;

        if (redisExecutionStrategy != null) {
            result = redisExecutionStrategy.getOrNull(configurationRequest, responseType);
        }

        if (result == null) {
            String accessToken = accessTokenProvider.acquireToken(configurationRequest.authCredentials());
            ClientHttpRequest httpRequest = createHttpRequest(configurationRequest, accessToken);
            String responseBody = executeHttpCall(httpRequest);
            result = jsonDeserializer.deserialize(responseBody, responseType);
        }

        if (result != null) {
            logger.debug("Successfully retrieved configuration for objectType='{}'{}",
                    configurationRequest.objectType(),
                    configurationRequest.objectId() != null ? ", objectId='" + configurationRequest.objectId() + "'" : "");
        } else {
            logger.warn("Configuration not found for objectType='{}'{}",
                    configurationRequest.objectType(),
                    configurationRequest.objectId() != null ? ", objectId='" + configurationRequest.objectId() + "'" : "");
            throw new SettingsClientException("Configuration not found for request: " + configurationRequest);
        }

        return result;
    }

    private String executeHttpCall(ClientHttpRequest request) {
        return httpExecutionStrategy.execute(request);
    }

    private ClientHttpRequest createHttpRequest(ConfigurationRequest configurationRequest, String accessToken) {
        String requestUrl = buildGetConfigurationsUrl(configurationRequest);

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HEADER_AUTHORIZATION, "Bearer " + accessToken);
        headers.put(HttpConstants.HEADER_CACHE_CONTROL, "no-cache");
        headers.put(HttpConstants.HEADER_ACCEPT, HttpConstants.CONTENT_TYPE_JSON);

        if (configurationRequest.rsaPrivateKey() != null && !configurationRequest.rsaPrivateKey().isBlank()) {
            headers.put(HttpConstants.HEADER_RSA_PRIVATE_KEY, configurationRequest.rsaPrivateKey());
        }
        if (configurationRequest.interactionId() != null && !configurationRequest.interactionId().isBlank()) {
            headers.put(HttpConstants.HEADER_INTERACTION_ID, configurationRequest.interactionId());
        }

        return new ClientHttpRequest(
                URI.create(requestUrl),
                HttpMethod.GET,
                headers,
                requestTimeout,
                null
        );
    }

    private String buildGetConfigurationsUrl(ConfigurationRequest configurationRequest) {
        switch (configurationRequest.objectType()) {
            case AGENT:
                return apiBaseUrl + "/" + SETTINGS_CONFIGURATIONS_PATH + "/" +
                        "1/" +
                        configurationRequest.service() + "/" +
                        configurationRequest.objectType().name().toLowerCase() + "/" +
                        configurationRequest.objectId();
            case ACCOUNT:
                //case RING_GROUP:
                return apiBaseUrl + "/" + SETTINGS_CONFIGURATIONS_PATH + "/" +
                        configurationRequest.service() + "/" +
                        configurationRequest.objectType().name().toLowerCase();
            default:
                throw new IllegalArgumentException("Configuration request '" + configurationRequest.objectType() + "' is not supported.");
        }
    }

}
