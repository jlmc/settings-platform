package io.github.jlmc.settings.client.adapters.redis;

import io.github.jlmc.settings.client.adapters.redis.keys.KeyBuilder;
import io.github.jlmc.settings.client.core.ConfigurationRequest;
import io.github.jlmc.settings.client.ports.out.DistributedConfigProvider;
import io.github.jlmc.settings.client.ports.out.JsonDeserializer;
import io.github.jlmc.settings.domain.components.ResolvedConfigurationAssembler;
import io.github.jlmc.settings.domain.entities.ResolvedConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * Production-ready execution strategy for Redis-based configurations.
 * Features improved error handling, logging, and defensive checks.
 */
public class RedisDistributedConfigProvider implements DistributedConfigProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisDistributedConfigProvider.class);

    private final RedisSettingsProvider redisSettingsProvider;
    private final KeyBuilder keyBuilder;
    private final JsonDeserializer jsonDeserializer;
    private final ResolvedConfigurationAssembler resolvedConfigurationAssembler;

    public RedisDistributedConfigProvider(RedisSettingsProvider redisSettingsProvider,
                                          KeyBuilder keyBuilder,
                                          JsonDeserializer jsonDeserializer,
                                          ResolvedConfigurationAssembler resolvedConfigurationAssembler) {
        // Defensive null checks for dependencies
        this.redisSettingsProvider = Objects.requireNonNull(redisSettingsProvider, "redisSettingsProvider is required");
        this.keyBuilder = Objects.requireNonNull(keyBuilder, "keyBuilder is required");
        this.jsonDeserializer = Objects.requireNonNull(jsonDeserializer, "jsonDeserializer is required");
        this.resolvedConfigurationAssembler = Objects.requireNonNull(resolvedConfigurationAssembler, "resolvedConfigurationAssembler is required");
    }

    @Override
    public <T> T getOrNull(ConfigurationRequest request, Class<T> responseType) {
        // Validate input parameters
        if (request == null || responseType == null) {
            LOGGER.warn("Received null request or responseType. Returning null.");
            return null;
        }


        try {
            String key = keyBuilder.build(request);
            LOGGER.debug("Attempting to fetch configuration for key: {}", key);

            String value = redisSettingsProvider.getValue(key);

            if (value == null || value.isBlank()) {
                LOGGER.debug("No configuration found in Redis for key: {}", key);
                return null;
            }

            // 1. Deserialize raw JSON from Redis to Domain Entity
            ResolvedConfiguration resolvedConfiguration = jsonDeserializer.deserialize(value, ResolvedConfiguration.class);
            if (resolvedConfiguration == null) {
                LOGGER.warn("Deserialization returned null for key: {}", key);
                return null;
            }

            // 2. Assemble/Decrypt the configuration using the provided RSA key
            Map<String, Object> assembled = this.resolvedConfigurationAssembler.assemble(
                    resolvedConfiguration,
                    request.rsaPrivateKey()
            );

            // 3. Convert the assembled Map into the requested Type T
            return jsonDeserializer.readValueAs(assembled, responseType);

        } catch (Exception e) {
            // Log as WARN/ERROR depending on your alerting needs.
            // We catch everything to prevent cache failures from breaking the main application flow.
            LOGGER.warn("Unexpected error retrieving configuration for request: {}. Message: {}",
                    request, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void close() {
        redisSettingsProvider.close();
    }
}
