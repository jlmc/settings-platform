package io.github.jlmc.settings.client.redis.providers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.lettuce.core.RedisClient;
import io.lettuce.core.TrackingArgs;
import io.lettuce.core.api.push.PushMessage;
import io.lettuce.core.support.caching.CacheAccessor;
import io.lettuce.core.support.caching.CacheFrontend;
import io.lettuce.core.support.caching.ClientSideCaching;

import java.util.concurrent.TimeUnit;

/**
 * Resilient L1/L2 Cache implementation.
 * Handles automatic re-activation of Client-Side Tracking after reconnections.
 */
public class RedisL1L2Caffeine extends AbstractRedisSettingsProvider {

    private final CacheFrontend<String, String> frontend;
    private final Cache<String, String> caffeineCache;
    private final TrackingArgs trackingArgs;

    public RedisL1L2Caffeine(RedisClient redisClient, String namespace, long ttl, TimeUnit unit, long maximumSize) {
        super(redisClient, namespace);

        this.caffeineCache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(ttl, unit)
                .build();

        // 1️⃣ Inicializa a conexão

        this.trackingArgs = TrackingArgs.Builder.enabled().bcast().prefixes(namespace + ":");

        // Set up invalidation logging
        this.connection.addListener((PushMessage message) -> {
            if (message.getType().equals("invalidate")) {
                logger.info("L1 Eviction triggered: {}", message.getContent());
            }
        });

        this.frontend = ClientSideCaching.create(CacheAccessor.forMap(caffeineCache.asMap()), connection);

        // Initial activation
        onReconnection();
    }

    @Override
    protected void onReconnection() {
        connection.sync().clientTracking(trackingArgs);
        caffeineCache.invalidateAll();
        logger.debug("L1 Caffeine Cache synchronized.");
    }

    @Override
    public String getValue(String key) {
        if (key == null || key.isBlank()) return null;
        try {
            return frontend.get(key);
        } catch (Exception e) {
            logger.info("Cache access failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            frontend.close();
        } catch (Exception e) {
            logger.warn("Error closing frontend: {}", e.getMessage());
        }
    }
}
