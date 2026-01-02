package io.github.jlmc.settings.client.redis.providers;

import io.github.jlmc.settings.client.redis.RedisSettingsProvider;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisChannelHandler;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionStateListener;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.protocol.ProtocolVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public abstract class AbstractRedisSettingsProvider implements RedisSettingsProvider {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final RedisClient client;
    protected final String namespace;
    protected final StatefulRedisConnection<String, String> connection;

    protected AbstractRedisSettingsProvider(RedisClient redisClient, String namespace) {
        this.client = redisClient;
        this.namespace = namespace;

        // 1. Mandatory: Force RESP3 for all implementations to support advanced features
        this.client.setOptions(ClientOptions.builder()
                .protocolVersion(ProtocolVersion.RESP3)
                .autoReconnect(true)
                .build());

        // 2. Setup Lifecycle Listener
        this.client.addListener(new RedisConnectionStateListener() {
            @Override
            public void onRedisConnected(RedisChannelHandler<?, ?> c, SocketAddress socketAddress) {
                logger.info("Redis connected/reconnected at {}. [Namespace: {}]", socketAddress, namespace);
                onReconnection();
            }

            @Override
            public void onRedisDisconnected(RedisChannelHandler<?, ?> c) {
                logger.warn("Redis connection lost. [Namespace: {}]", namespace);
            }
        });

        this.connection = client.connect();
    }

    /**
     * Hook for subclasses to re-establish tracking or clear local caches upon reconnection.
     */
    protected abstract void onReconnection();

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public boolean isAvailable() {
        return connection != null && connection.isOpen();
    }

    @Override
    public void close() {
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }
}
