package io.github.jlmc.settings.service.adapters.sharedcache;

import io.github.jlmc.settings.service.domain.events.ConfigurationHitEvent;
import io.github.jlmc.settings.service.domain.events.SettingsAccountDeletedEvent;
import io.github.jlmc.settings.service.domain.events.SettingsAccountUpdatedEvent;

public interface SharedCacheSynchronizer {
    void update(SettingsAccountUpdatedEvent event);

    void delete(SettingsAccountDeletedEvent event);

    void hit(ConfigurationHitEvent event);

}
