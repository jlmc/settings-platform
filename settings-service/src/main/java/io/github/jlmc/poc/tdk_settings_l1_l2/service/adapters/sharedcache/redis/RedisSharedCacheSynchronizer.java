package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache.redis;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache.SharedCacheSynchronizer;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ResolvedConfiguration;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.ConfigurationHitEvent;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.SettingsAccountDeletedEvent;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.SettingsAccountUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class RedisSharedCacheSynchronizer implements SharedCacheSynchronizer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration ttl;
    private final String namespace;

    public RedisSharedCacheSynchronizer(RedisTemplate<String, Object> redisTemplate,
                                        Duration ttl,
                                        String namespace) {
        this.redisTemplate = redisTemplate;
        this.ttl = ttl;
        this.namespace = namespace;
    }

    @Override
    public void update(SettingsAccountUpdatedEvent event) {
        SettingsAccount settingsAccount = event.settingsAccount();
        log.debug("Updating shared cache for accountId='{}', type='{}', serviceName='{}' - REDIS",
                settingsAccount.accountId(), settingsAccount.type(), settingsAccount.serviceName());

        String baseKey = key(settingsAccount.accountId(), settingsAccount.serviceName(), settingsAccount.type());
        DeleteKeysResult deleted = deleteKeysByPattern(baseKey + "*");

        log.debug("Deleted {} keys before update for baseKey='{}'", deleted, baseKey);
    }

    @Override
    public void delete(SettingsAccountDeletedEvent event) {
        SettingsAccount settingsAccount = event.settingsAccount();

        log.debug("Deleting shared cache for accountId='{}', type='{}', serviceName='{}' - REDIS",
                settingsAccount.accountId(), settingsAccount.type(), settingsAccount.serviceName());

        String baseKey = key(settingsAccount.accountId(), settingsAccount.serviceName(), settingsAccount.type());
        DeleteKeysResult deleted = deleteKeysByPattern(baseKey + "*");

        log.debug("Deleted {} keys for baseKey='{}'", deleted, baseKey);
    }

    @Override
    public void hit(ConfigurationHitEvent event) {
        ResolvedConfiguration resolvedConfiguration = event.resolvedConfiguration();

        redisTemplate.opsForValue()
                .set(key(resolvedConfiguration.accountId(), resolvedConfiguration.serviceName(), resolvedConfiguration.configurationType()), resolvedConfiguration, ttl);
    }

    private DeleteKeysResult deleteKeysByPattern(String pattern) {
        return redisTemplate.execute((RedisCallback<DeleteKeysResult>) connection -> {
            long totalDeleted = 0;
            Set<String> deletedKeys = new HashSet<>();

            try {
                // Using SCAN for safety in production
                try (var cursor = connection.keyCommands().scan(
                        ScanOptions.scanOptions().match(pattern).count(1000).build()
                )) {
                    while (cursor.hasNext()) {
                        byte[] key = cursor.next();
                        totalDeleted += connection.keyCommands().del(key);
                        deletedKeys.add(new String(key));
                    }
                }
            } catch (Exception e) {
                log.error("Error deleting keys with pattern='{}'", pattern, e);
                throw new RuntimeException("Failed to delete keys with pattern=" + pattern, e);
            }

            return new DeleteKeysResult(totalDeleted, deletedKeys);
        });
    }

    private String key(String accountId, String serviceName, ConfigurationType configurationType) {
        return String.join(":", namespace, accountId, serviceName, configurationType.name());
    }

    record DeleteKeysResult(long deletedCount, Collection<String> deletedKeys) {}
}
