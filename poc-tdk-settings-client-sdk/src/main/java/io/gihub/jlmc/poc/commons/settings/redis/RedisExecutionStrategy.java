package io.gihub.jlmc.poc.commons.settings.redis;

import io.gihub.jlmc.poc.commons.settings.ConfigurationRequest;
import io.gihub.jlmc.poc.commons.settings.json.JsonDeserializer;
import io.gihub.jlmc.poc.commons.settings.redis.sdk.contracts.ObjectNodeMerger;

import java.util.function.Function;

public interface RedisExecutionStrategy {

    static RedisExecutionStrategy createDefault(
            JsonDeserializer jsonDeserializer,
            String redisUrl,
            String namespace,
            Function<ConfigurationRequest, String> accountIdProvider,
            ObjectNodeMerger objectNodeMerger) {
        RedisL1SimpleMap redisL1SimpleMap = new RedisL1SimpleMap(redisUrl, namespace);
        io.gihub.jlmc.poc.commons.settings.redis.keys.StandardKeyBuilder keyBuilder =
                new io.gihub.jlmc.poc.commons.settings.redis.keys.StandardKeyBuilder(namespace, accountIdProvider);


        //
        return new DefaultRedisExecutionStrategy(redisL1SimpleMap, keyBuilder, jsonDeserializer, objectNodeMerger);
    }

    <T> T getOrNull(
            ConfigurationRequest request,
            Class<T> responseType
    );


}
