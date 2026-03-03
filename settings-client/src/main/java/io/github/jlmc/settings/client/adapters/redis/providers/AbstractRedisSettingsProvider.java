package io.github.jlmc.settings.client.adapters.redis.providers;

import io.github.jlmc.settings.client.adapters.redis.RedisSettingsProvider;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisChannelHandler;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionStateListener;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.protocol.ProtocolVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Objects;

public abstract class AbstractRedisSettingsProvider implements RedisSettingsProvider {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final RedisClient client;
    protected final String namespace;
    protected final StatefulRedisConnection<String, String> connection;

    protected AbstractRedisSettingsProvider(RedisClient redisClient, String namespace) {
        this.client = Objects.requireNonNull(redisClient, "RedisClient cannot be null");
        this.namespace = Objects.requireNonNull(namespace, "Namespace cannot be null");

        // Force RESP3 for Server-Side Tracking support
        // 1. Mandatory: Force RESP3 for all implementations to support advanced features
        this.client.setOptions(ClientOptions.builder()
                .protocolVersion(ProtocolVersion.RESP3)
                .autoReconnect(true)
                .build());

        // Setup Listener BEFORE connecting
        this.client.addListener(new RedisConnectionStateListener() {
            @Override
            public void onRedisConnected(RedisChannelHandler<?, ?> c, SocketAddress socketAddress) {
                logger.info("Redis connected at {}. [Namespace: {}]", socketAddress, namespace);
                // Subclasses will re-enable tracking here
                if (connection != null) {
                    onReconnection();
                }
            }

            @Override
            public void onRedisDisconnected(RedisChannelHandler<?, ?> c) {
                logger.warn("Redis connection lost. [Namespace: {}]", namespace);
            }
        });

        this.connection = client.connect();
    }

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
        logger.info("Closing Redis provider for namespace: {}", namespace);
        if (connection != null) {
            connection.close();
        }
        // Important: Shutdown the client to release resources/Netty threads
        // Note: Only do this if this provider 'owns' the client lifecycle.
        if (client != null) {
            client.shutdown();
        }
    }
}
