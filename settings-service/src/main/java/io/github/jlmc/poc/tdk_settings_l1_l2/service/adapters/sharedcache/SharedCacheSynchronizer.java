package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.ConfigurationHitEvent;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.SettingsAccountDeletedEvent;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.SettingsAccountUpdatedEvent;

public interface SharedCacheSynchronizer {
    void update(SettingsAccountUpdatedEvent event);

    void delete(SettingsAccountDeletedEvent event);

    void hit(ConfigurationHitEvent event);

}
