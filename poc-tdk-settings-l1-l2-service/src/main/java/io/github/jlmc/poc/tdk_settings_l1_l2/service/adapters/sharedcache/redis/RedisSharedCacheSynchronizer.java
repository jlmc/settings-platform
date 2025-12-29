package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache.redis;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SharedCacheSynchronizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
public class RedisSharedCacheSynchronizer implements SharedCacheSynchronizer {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisSharedCacheSynchronizer(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void update(SettingsAccount settingsAccount) {
      log.debug("updating shared cache with SettingsAccount for accountId='{}', type='{}', serviceName='{}' - REDIS",
              settingsAccount.accountId(), settingsAccount.type(), settingsAccount.serviceName());
    }

    @Override
    public void delete(SettingsAccount settingsAccount) {
        log.debug("deleting SettingsAccount from shared cache for accountId='{}', type='{}', serviceName='{}' - REDIS",
                settingsAccount.accountId(), settingsAccount.type(), settingsAccount.serviceName());
        redisTemplate.delete(key(settingsAccount.accountId(), settingsAccount.serviceName(), settingsAccount.type()));
    }

    @Override
    public void hit(HitData hitData) {
        redisTemplate.opsForValue()
                .set(key(hitData.accountId(), hitData.serviceName(), hitData.configurationType()), hitData);
    }

    private String key(String accountId, String serviceName, ConfigurationType configurationType) {
        return "settings-account:" + accountId + ":" + ":" + serviceName + ":" + configurationType.name();
    }
}
