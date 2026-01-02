package io.gihub.jlmc.poc.commons.settings.redis;

import io.gihub.jlmc.poc.commons.settings.ConfigurationRequest;
import io.gihub.jlmc.poc.commons.settings.json.JsonDeserializer;
import io.gihub.jlmc.poc.commons.settings.redis.keys.KeyBuilder;
import io.github.jlmc.poc.settings.sdk.domain.ResolvedConfigurationAssembler;
import io.github.jlmc.poc.settings.sdk.domain.entities.ResolvedConfiguration;

import java.util.Map;

public class DefaultRedisExecutionStrategy implements RedisExecutionStrategy {

    private final RedisL1SimpleMap redisL1SimpleMap;
    private final KeyBuilder keyBuilder;
    private final JsonDeserializer jsonDeserializer;
    private final ResolvedConfigurationAssembler resolvedConfigurationAssembler;

    public DefaultRedisExecutionStrategy(RedisL1SimpleMap redisL1SimpleMap,
                                         KeyBuilder keyBuilder,
                                         JsonDeserializer jsonDeserializer,
                                         ResolvedConfigurationAssembler resolvedConfigurationAssembler) {
        this.redisL1SimpleMap = redisL1SimpleMap;
        this.keyBuilder = keyBuilder;
        this.jsonDeserializer = jsonDeserializer;
        this.resolvedConfigurationAssembler = resolvedConfigurationAssembler;
    }

    @Override
    public <T> T getOrNull(ConfigurationRequest request, Class<T> responseType) {
        String key = keyBuilder.build(request);
        String value = redisL1SimpleMap.getValue(key);

        if (value == null || value.isBlank()) {
            return null;
        }

        ResolvedConfiguration resolvedConfiguration = jsonDeserializer.deserialize(value, ResolvedConfiguration.class);
        Map<String, Object> assembled = this.resolvedConfigurationAssembler.assemble(resolvedConfiguration, request.rsaPrivateKey());
        return jsonDeserializer.readValueAs(assembled, responseType);
    }
}
