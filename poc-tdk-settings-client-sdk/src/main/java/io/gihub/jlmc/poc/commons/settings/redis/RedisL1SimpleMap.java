package io.gihub.jlmc.poc.commons.settings.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.caching.CacheAccessor;
import io.lettuce.core.support.caching.CacheFrontend;
import io.lettuce.core.support.caching.ClientSideCaching;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisL1SimpleMap {

    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;
    private final CacheFrontend<String, String> frontend;
    private final Map<String, String> simpleMap;

    public RedisL1SimpleMap(String redisUrl) {
        if (redisUrl == null || redisUrl.isBlank()) {
            throw new IllegalArgumentException("redisUrl cannot be null or blank");
        }
        // 1. Configuração do Cliente e Conexão L2 (Redis)
        this.client = RedisClient.create(redisUrl);
        this.connection = client.connect();

        // L1: Apenas um mapa thread-safe comum
        // ATENÇÃO: Se o Redis tiver 1GB de dados, este mapa tentará ter 1GB na RAM da App
        this.simpleMap = new ConcurrentHashMap<>();

        // Unindo L1 (Map) com L2 (Redis)
        this.frontend = ClientSideCaching.create(
                CacheAccessor.forMap(simpleMap),
                connection
        );
    }

    public String getValue(String key) {
        // Tenta L1 -> Se Miss -> Vai L2 -> Popula L1 -> Retorna
        return frontend.get(key);
    }

    public void close() {
        if (connection != null) {
            connection.close();
        }
        if (client != null) {
            client.shutdown();
        }
    }
}
