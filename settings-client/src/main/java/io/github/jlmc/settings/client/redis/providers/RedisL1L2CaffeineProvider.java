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
public class RedisL1L2CaffeineProvider extends AbstractRedisSettingsProvider {

    protected final CacheFrontend<String, String> frontend;
    protected final Cache<String, String> caffeineCache;
    protected final TrackingArgs trackingArgs;

    public RedisL1L2CaffeineProvider(RedisClient redisClient,
                                     String namespace,
                                     long ttl,
                                     TimeUnit unit,
                                     long maximumSize) {
        super(redisClient, namespace);

        this.caffeineCache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(ttl, unit)
                .build();

        // Broadcast mode (BCAST) is efficient for many keys under one prefix
        this.trackingArgs = TrackingArgs.Builder.enabled()
                .bcast()
                .prefixes(namespace + ":");

        // Monitoring Push Messages
        this.connection.addListener((PushMessage message) -> {
            if (message.getType().equals("invalidate")) {
                logger.debug("L1 Eviction event received from Redis: {}", message.getContent());
            }
        });

        this.frontend = ClientSideCaching.create(
                CacheAccessor.forMap(caffeineCache.asMap()),
                connection
        );

        // Explicitly trigger the first activation
        this.onReconnection();
    }

    @Override
    protected void onReconnection() {
        try {
            if (connection.isOpen()) {
                // Re-enable tracking on the new connection
                connection.sync().clientTracking(trackingArgs);
                // Crucial: Clear L1 because we don't know what changed during downtime
                caffeineCache.invalidateAll();
                logger.info("L1 Tracking re-activated and cache invalidated for namespace: {}", namespace);
            }
        } catch (Exception e) {
            logger.error("Failed to re-activate tracking for namespace {}: {}", namespace, e.getMessage());
        }
    }

    @Override
    public String getValue(String key) {
        if (key == null || key.isBlank()) return null;
        try {
            // Frontend handles: L1 check -> L2 fetch -> L1 populate
            // BUT: CacheFrontend.get(key) uses the EXACT key provided.
            // Our Redis keys are prefixed with "namespace:".
            //String redisKey = namespace + ":" + key;
            return frontend.get(key);
        } catch (Exception e) {
            // Catching all to ensure high availability (return null so business can use fallback)
            logger.warn("Cache fetch failed for key {}. Returning null. Error: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void close() {
        try {
            // 1. Close the frontend first (removes internal listeners)
            if (frontend != null) {
                frontend.close();
            }
        } catch (Exception e) {
            logger.error("Error closing CacheFrontend", e);
        } finally {
            // 2. Close connection and shutdown client via super
            super.close();
        }
    }
}
