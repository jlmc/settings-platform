package io.github.jlmc.poc.commons.settings.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisChannelHandler;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionStateListener;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.protocol.ProtocolVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 * L2-Only implementation.
 * Direct access to Redis without local memory storage (L1).
 * Suitable for data that changes frequently across multiple instances.
 */
public class RedisL2OnlyService implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisL2OnlyService.class);

    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;
    private final String namespace;

    public RedisL2OnlyService(RedisClient redisClient, String namespace) {
        this.client = redisClient;
        this.namespace = namespace;

        // 1. Basic configuration (RESP3 is not strictly required here, but recommended)
        this.client.setOptions(ClientOptions.builder()
                .protocolVersion(ProtocolVersion.RESP3)
                .autoReconnect(true)
                .build());

        // 2. Connectivity monitoring
        this.client.addListener(new RedisConnectionStateListener() {
            @Override
            public void onRedisConnected(RedisChannelHandler<?, ?> c, SocketAddress socketAddress) {
                LOGGER.info("Redis L2 connection established at {}", socketAddress);
            }

            @Override
            public void onRedisDisconnected(RedisChannelHandler<?, ?> c) {
                LOGGER.warn("Redis L2 connection lost. Application will attempt to recover...");
            }
        });

        this.connection = client.connect();
    }

    /**
     * Directly fetches the value from Redis.
     * Every call results in a network round-trip.
     */
    public String getValue(String key) {
        if (key == null || key.isBlank()) return null;

        try {
            // Direct L2 (Redis) access
            return connection.sync().get(key);
        } catch (Exception e) {
            // Log as WARN to allow graceful fallback to DB in the business layer
            LOGGER.info("Failed to fetch key {} from Redis L2. Reason: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void close() {
        if (connection != null) connection.close();
        if (client != null) client.shutdown();
    }
}
