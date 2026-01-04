package io.github.jlmc.settings.client.redis.providers;

import io.lettuce.core.RedisClient;
import io.lettuce.core.TrackingArgs;
import io.lettuce.core.api.push.PushMessage;
import io.lettuce.core.support.caching.CacheAccessor;
import io.lettuce.core.support.caching.CacheFrontend;
import io.lettuce.core.support.caching.ClientSideCaching;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resilient L1/L2 cache implementation using a concurrent Map.
 * Note: This version is recommended only for small, finite data sets (preferably under a few thousand entries), for example 1000s of keys,
 * as it does not provide automatic eviction or local TTL policies.
 */
public class RedisL1L2SimpleMapProvider extends AbstractRedisSettingsProvider {

    protected final CacheFrontend<String, String> frontend;
    protected final Map<String, String> mapCache;
    protected final TrackingArgs trackingArgs;

    public RedisL1L2SimpleMapProvider(RedisClient redisClient, String namespace) {
        super(redisClient, namespace);

        this.mapCache = new ConcurrentHashMap<>();

        // Tracking configuration with Broadcast (BCAST)
        this.trackingArgs = TrackingArgs.Builder.enabled()
                .bcast()
                .prefixes(namespace + ":");

        // Invalidation event monitoring
        this.connection.addListener((PushMessage message) -> {
            if (message.getType().equals("invalidate")) {
                logger.debug("L1 SimpleMap Invalidation received: {}", message.getContent());
                // For BCAST mode, we might get prefix invalidations.
                // Lettuce's CacheFrontend might handle this, but let's be sure.
            }
        });

        // // Frontend creation connecting the Map to the Redis connection
        this.frontend = ClientSideCaching.create(
                CacheAccessor.forMap(mapCache),
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
                mapCache.clear();
                logger.info("L1 Simple Map re-activated and cleared for namespace: {}", namespace);
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
            return frontend.get(key);
        } catch (Exception e) {
            logger.warn("Cache access failed for key {}. Returning null. Error: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void close() {
        try {
            // 1. Close the frontend first (removes internal listeners)
            if (frontend != null) {
                // Crucial: Removes internal Lettuce listeners
                frontend.close();
            }
            mapCache.clear();
        } catch (Exception e) {
            logger.error("Error closing CacheFrontend in SimpleMap provider", e);
        } finally {
            // 2. Close connection and shutdown client via super
            super.close();
        }
    }
}
