package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache.redis;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SharedCacheSynchronizer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisSharedCacheSynchronizer implements SharedCacheSynchronizer {
    @Override
    public void update(SettingsAccount settingsAccount) {
      log.debug("updating shared cache with SettingsAccount for accountId='{}', type='{}', serviceName='{}' - REDIS",
              settingsAccount.accountId(), settingsAccount.type(), settingsAccount.serviceName());
    }

    @Override
    public void delete(SettingsAccount settingsAccount) {
        //TODO: implement Redis delete logic
    }
}
