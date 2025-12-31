package io.gihub.jlmc.poc.commons.settings.redis;

import io.gihub.jlmc.poc.commons.settings.ConfigurationRequest;
import io.gihub.jlmc.poc.commons.settings.json.JsonDeserializer;

import java.util.function.Function;

public interface RedisExecutionStrategy {

    static RedisExecutionStrategy createDefault(
            JsonDeserializer jsonDeserializer,
            String redisAddress,
            String redisPassword,
            boolean cluster,
            String namespace,
            Function<ConfigurationRequest, String> accountIdProvider) {
        // TODO: Implement the method to create a default RedisExecutionStrategy
        return null;
    }

    <T> T getOrNull(
            ConfigurationRequest request,
            Class<T> responseType
    );


}
