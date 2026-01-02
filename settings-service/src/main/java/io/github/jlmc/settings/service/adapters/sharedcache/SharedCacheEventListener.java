package io.github.jlmc.settings.service.adapters.sharedcache;

import io.github.jlmc.settings.service.domain.entities.ResolvedConfiguration;
import io.github.jlmc.settings.service.domain.events.ConfigurationHitEvent;
import io.github.jlmc.settings.service.domain.events.SettingsAccountDeletedEvent;
import io.github.jlmc.settings.service.domain.events.SettingsAccountUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SharedCacheEventListener {

    private final SharedCacheSynchronizer sharedCacheSynchronizer;

    public SharedCacheEventListener(SharedCacheSynchronizer sharedCacheSynchronizer) {
        this.sharedCacheSynchronizer = sharedCacheSynchronizer;
    }

    @EventListener
    public void onConfigurationHit(ConfigurationHitEvent event) {
        ResolvedConfiguration resolvedConfiguration = event.resolvedConfiguration();
        log.debug("Handling ConfigurationHitEvent for account={}, service={}",
                resolvedConfiguration.accountId(), resolvedConfiguration.serviceName());
        sharedCacheSynchronizer.hit(event);
    }

    @EventListener
    public void onSettingsAccountUpdated(SettingsAccountUpdatedEvent event) {
        log.debug("Handling SettingsAccountUpdatedEvent for account={}, service={}, type={}",
                event.settingsAccount().accountId(), event.settingsAccount().serviceName(), event.settingsAccount().type());
        sharedCacheSynchronizer.update(event);
    }

    @EventListener
    public void onSettingsAccountDeleted(SettingsAccountDeletedEvent event) {
        log.debug("Handling SettingsAccountDeletedEvent for account={}, service={}, type={}",
                event.settingsAccount().accountId(), event.settingsAccount().serviceName(), event.settingsAccount().type());
        sharedCacheSynchronizer.delete(event);
    }
}
