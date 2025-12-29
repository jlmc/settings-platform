package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;

public interface SharedCacheSynchronizer {
    void update(SettingsAccount settingsAccount);
    void delete(SettingsAccount settingsAccount);
}
