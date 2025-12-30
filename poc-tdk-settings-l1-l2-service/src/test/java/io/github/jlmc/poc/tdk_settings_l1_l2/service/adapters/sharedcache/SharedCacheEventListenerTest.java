package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.ConfigurationHitEvent;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.SettingsAccountDeletedEvent;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.SettingsAccountUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SharedCacheEventListenerTest {

    @Mock
    private SharedCacheSynchronizer sharedCacheSynchronizer;

    @InjectMocks
    private SharedCacheEventListener victim;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void onConfigurationHit() {
        ConfigurationHitEvent event = new ConfigurationHitEvent(
                "acc1", "srv1", ConfigurationType.ACCOUNT, List.of(), Map.of());

        victim.onConfigurationHit(event);

        verify(sharedCacheSynchronizer).hit(event);
    }

    @Test
    void onSettingsAccountUpdated() {
        SettingsAccount settingsAccount = new SettingsAccount(ConfigurationType.ACCOUNT, "acc1", "srv1", objectMapper.createObjectNode());
        SettingsAccountUpdatedEvent event = new SettingsAccountUpdatedEvent(settingsAccount);

        victim.onSettingsAccountUpdated(event);

        verify(sharedCacheSynchronizer).update(event);
    }

    @Test
    void onSettingsAccountDeleted() {
        SettingsAccount settingsAccount = new SettingsAccount(ConfigurationType.ACCOUNT, "acc1", "srv1", objectMapper.createObjectNode());
        SettingsAccountDeletedEvent event = new SettingsAccountDeletedEvent(settingsAccount);

        victim.onSettingsAccountDeleted(event);

        verify(sharedCacheSynchronizer).delete(event);
    }
}
