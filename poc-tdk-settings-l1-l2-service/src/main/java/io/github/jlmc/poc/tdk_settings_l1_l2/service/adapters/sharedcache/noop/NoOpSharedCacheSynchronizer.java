package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache.noop;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache.SharedCacheSynchronizer;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ResolvedConfiguration;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.ConfigurationHitEvent;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.SettingsAccountDeletedEvent;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.SettingsAccountUpdatedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpSharedCacheSynchronizer implements SharedCacheSynchronizer {

    @Override
    public void update(SettingsAccountUpdatedEvent event) {
        SettingsAccount settingsAccount = event.settingsAccount();
        log.debug("updating shared cache with SettingsAccount for accountId='{}', type='{}', serviceName='{}' - NO-OP",
                settingsAccount.accountId(), settingsAccount.type(), settingsAccount.serviceName());
    }

    @Override
    public void delete(SettingsAccountDeletedEvent event) {
        SettingsAccount settingsAccount = event.settingsAccount();
        log.debug("deleting from shared cache SettingsAccount for accountId='{}', type='{}', serviceName='{}' - NO-OP",
                settingsAccount.accountId(), settingsAccount.type(), settingsAccount.serviceName());
    }

    @Override
    public void hit(ConfigurationHitEvent event) {
        ResolvedConfiguration resolvedConfiguration = event.resolvedConfiguration();

        log.debug("hit shared cache with {} SettingsAccounts and mergedSettings of size {} - NO-OP",
                resolvedConfiguration.settingsAccounts().size(),
                resolvedConfiguration.mergedSettings().size());
    }
}
