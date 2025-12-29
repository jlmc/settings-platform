package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;

import java.util.Optional;

public interface SettingsAccountRepository {
    SettingsAccount save(SettingsAccount entity);
    Optional<SettingsAccount> find(String accountId, String serviceName, ConfigurationType type);
    void delete(SettingsAccount settingsAccount);
}
