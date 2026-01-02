package io.gihub.jlmc.poc.commons.settings.redis;

import io.gihub.jlmc.poc.commons.settings.ConfigurationRequest;
import io.gihub.jlmc.poc.commons.settings.json.JsonDeserializer;
import io.gihub.jlmc.poc.commons.settings.redis.keys.KeyBuilder;
import io.gihub.jlmc.poc.commons.settings.redis.sdk.contracts.ObjectNodeMerger;
import io.gihub.jlmc.poc.commons.settings.redis.sdk.model.ResolvedConfiguration;
import io.gihub.jlmc.poc.commons.settings.redis.sdk.model.SettingsAccount;

import java.util.List;
import java.util.Map;

import static io.gihub.jlmc.poc.commons.settings.redis.sdk.model.SettingsAccount.asSuppliers;

public class DefaultRedisExecutionStrategy implements RedisExecutionStrategy {

    private final RedisL1SimpleMap redisL1SimpleMap;
    private final KeyBuilder keyBuilder;
    private final JsonDeserializer jsonDeserializer;
    private final ObjectNodeMerger objectNodeMerger;

    public DefaultRedisExecutionStrategy(RedisL1SimpleMap redisL1SimpleMap,
                                         KeyBuilder keyBuilder,
                                         JsonDeserializer jsonDeserializer,
                                         ObjectNodeMerger objectNodeMerger) {
        this.redisL1SimpleMap = redisL1SimpleMap;
        this.keyBuilder = keyBuilder;
        this.jsonDeserializer = jsonDeserializer;
        this.objectNodeMerger = objectNodeMerger;
    }

    @Override
    public <T> T getOrNull(ConfigurationRequest request, Class<T> responseType) {
        String key = keyBuilder.build(request);
        String value = redisL1SimpleMap.getValue(key);

        if (value == null || value.isBlank()) {
            return null;
        }

        //
        ResolvedConfiguration resolvedConfiguration = jsonDeserializer.deserialize(value, ResolvedConfiguration.class);
        Map<String, Object> stringObjectMap = mergeSettings(resolvedConfiguration.settingsAccounts());

        T t = jsonDeserializer.readValueAs(stringObjectMap, responseType);
        //

        return t;
    }

    private Map<String, Object> mergeSettings(List<SettingsAccount> settings) {
        return objectNodeMerger.mergeContentsAsMap(asSuppliers(settings));
    }
}
