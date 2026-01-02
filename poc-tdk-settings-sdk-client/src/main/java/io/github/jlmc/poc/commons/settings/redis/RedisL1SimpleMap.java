package io.github.jlmc.poc.commons.settings.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.TrackingArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.caching.CacheAccessor;
import io.lettuce.core.support.caching.CacheFrontend;
import io.lettuce.core.support.caching.ClientSideCaching;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisL1SimpleMap {

    private final RedisClient client;
    private StatefulRedisConnection<String, String> connection;
    private CacheFrontend<String, String> frontend;
    private Map<String, String> simpleMap;

    public RedisL1SimpleMap(String redisUrl, String namespace) {
        if (redisUrl == null || redisUrl.isBlank()) {
            throw new IllegalArgumentException("redisUrl cannot be null or blank");
        }
        // 1. Configuração do Cliente e Conexão L2 (Redis)
        this.client = RedisClient.create(redisUrl);

        // L1: Apenas um mapa thread-safe comum
        init(namespace);
    }

    public RedisL1SimpleMap(RedisClient redisClient, String namespace) {
        // 1. Configuração do Cliente e Conexão L2 (Redis)
        this.client = redisClient;

        // L1: Apenas um mapa thread-safe comum
        init(namespace);
    }

    void init(String namespace) {
        // L1: Apenas um mapa thread-safe comum
        // ATENÇÃO: Se o Redis tiver 1GB de dados, este mapa tentará ter 1GB na RAM da App
        this.simpleMap = new ConcurrentHashMap<>();

        // Unindo L1 (Map) com L2 (Redis)
        TrackingArgs trackingArgs = TrackingArgs.Builder.enabled()
                .bcast() // <--- ESSENCIAL AQUI
                .prefixes(namespace + ":");

        this.connection = client.connect();
        // 1. Ativa o rastreamento no servidor para ESTA conexão específica
        // Isso diz ao Redis: "Mande mensagens de invalidação para este cliente"
        connection.sync().clientTracking(trackingArgs);

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
