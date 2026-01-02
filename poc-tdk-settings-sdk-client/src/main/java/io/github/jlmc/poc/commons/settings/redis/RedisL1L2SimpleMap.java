package io.github.jlmc.poc.commons.settings.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisChannelHandler;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionStateListener;
import io.lettuce.core.TrackingArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.support.caching.CacheAccessor;
import io.lettuce.core.support.caching.CacheFrontend;
import io.lettuce.core.support.caching.ClientSideCaching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Resilient L1/L2 Cache implementation.
 * Handles automatic re-activation of Client-Side Tracking after reconnections.
 */
public class RedisL1L2SimpleMap implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisL1L2Caffeine.class);

    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;
    private final CacheFrontend<String, String> frontend;
    private final Map<String, String> simpleMapCache;
    private final TrackingArgs trackingArgs;

    public RedisL1L2SimpleMap(RedisClient redisClient,
                             String namespace,
                             long ttlDuration,
                             TimeUnit ttlUnit) {
        this.client = redisClient;

        // 1. Force RESP3 (Mandatory for push notifications)
        this.client.setOptions(ClientOptions.builder()
                .protocolVersion(ProtocolVersion.RESP3)
                .build());

        // 2. Setup L1 Storage
        simpleMapCache = new ConcurrentHashMap<>();

        // 3. Define Tracking Arguments
        this.trackingArgs = TrackingArgs.Builder.enabled()
                .bcast()
                .prefixes(namespace + ":");

        // 4. Setup Connection State Listener for Auto-Recovery
        //  Add the state listener BEFORE connecting
        this.client.addListener(new RedisConnectionStateListener() {
            @Override
            public void onRedisConnected(RedisChannelHandler<?, ?> c, SocketAddress socketAddress) {
                LOGGER.info("Redis connected/reconnected at {}. Re-activating Tracking...", socketAddress);

                // IMPORTANT: Re-enable tracking after connection is established
                connection.sync().clientTracking(trackingArgs);

                // Safety measure: Clear L1 to avoid stale data from downtime period
                simpleMapCache.clear();
                LOGGER.debug("L1 Cache flushed to ensure synchronization.");
            }

            @Override
            public void onRedisDisconnected(RedisChannelHandler<?, ?> c) {
                LOGGER.warn("Redis L2 connection lost. Application will attempt to recover...");
            }
        });

        this.connection = client.connect();

        // 5. Setup Invalidation Listener for observability
        this.connection.addListener(message -> {
            if (message.getType().equals("invalidate")) {
                LOGGER.info("L1 Eviction triggered by Redis: {}", message.getContent());
            }
        });

        // 6. Create Frontend
        this.frontend = ClientSideCaching.create(
                CacheAccessor.forMap(simpleMapCache),
                connection
        );
    }

    public String getValue(String key) {
        if (key == null || key.isBlank()) return null;
        try {
            return frontend.get(key);
        } catch (Exception e) {
            LOGGER.info("Cache unavailable for key {}. Reason: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void close() {
        if (connection != null) connection.close();
        if (client != null) client.shutdown();
    }
}
