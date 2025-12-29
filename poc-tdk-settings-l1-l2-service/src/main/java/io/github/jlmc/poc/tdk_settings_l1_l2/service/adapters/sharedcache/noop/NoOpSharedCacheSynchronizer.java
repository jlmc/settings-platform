package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache.noop;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SharedCacheSynchronizer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpSharedCacheSynchronizer implements SharedCacheSynchronizer {

    @Override
    public void update(SettingsAccount entity) {
        log.debug("updating shared cache with SettingsAccount for accountId='{}', type='{}', serviceName='{}' - NO-OP",
                entity.accountId(), entity.type(), entity.serviceName());
    }

    @Override
    public void delete(SettingsAccount settingsAccount) {
        log.debug("deleting from shared cache SettingsAccount for accountId='{}', type='{}', serviceName='{}' - NO-OP",
                settingsAccount.accountId(), settingsAccount.type(), settingsAccount.serviceName());
    }
}
