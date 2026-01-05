package io.github.jlmc.settings.client.adapters.redis.providers;

import io.lettuce.core.RedisClient;

/**
 * L2-Only implementation.
 * Direct access to Redis without local memory storage (L1).
 * Suitable for data that changes frequently across multiple instances
 * and where the key is already fully qualified.
 */
public class RedisL2OnlyProvider extends AbstractRedisSettingsProvider {

    public RedisL2OnlyProvider(RedisClient redisClient, String namespace) {
        super(redisClient, namespace);
    }

    @Override
    protected void onReconnection() {
        // L2-Only does not use Client Tracking (RESP3 Push notifications),
        // we just log the availability for monitoring purposes.
        logger.info("Redis L2-Only connection re-established for namespace: {}", namespace);
    }

    @Override
    public String getValue(String key) {
        // Defensive check for null or blank keys
        if (key == null || key.isBlank()) {
            return null;
        }

        // Defensive connectivity check
        if (!isAvailable()) {
            logger.warn("Redis L2 is not available. Skipping fetch for key: {}", key);
            return null;
        }

        try {
            // Direct L2 (Redis) access.
            // As per requirements, the key is already fully qualified (no concatenation needed).
            return connection.sync().get(key);

        } catch (Exception e) {
            // Log as WARN for monitoring, but fail gracefully
            logger.warn("Failed to fetch key [{}] from Redis L2: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void close() {
        // Lifecycle management is delegated to the base class
        super.close();
    }
}
