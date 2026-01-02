package io.github.jlmc.poc.commons.settings.redis.providers;

import io.lettuce.core.RedisClient;

/**
 * L2-Only implementation.
 * Direct access to Redis without local memory storage (L1).
 * Suitable for data that changes frequently across multiple instances.
 */
public class RedisL2OnlyService extends AbstractRedisSettingsProvider {

    public RedisL2OnlyService(RedisClient redisClient, String namespace) {
        super(redisClient, namespace);
    }

    @Override
    protected void onReconnection() {
        // No tracking needed for L2-only
        logger.debug("L2 connection ready.");
    }

    @Override
    public String getValue(String key) {
        if (key == null || key.isBlank()) return null;
        try {
            return connection.sync().get(key);
        } catch (Exception e) {
            logger.info("L2 access failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }
}
