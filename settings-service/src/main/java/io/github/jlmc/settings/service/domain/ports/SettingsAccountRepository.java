package io.github.jlmc.settings.service.domain.ports;

import io.github.jlmc.settings.service.domain.entities.ConfigurationType;
import io.github.jlmc.settings.service.domain.entities.SettingsAccount;

import java.util.List;
import java.util.Optional;

public interface SettingsAccountRepository {

    Optional<SettingsAccount> find(String accountId, String serviceName, ConfigurationType type);

    List<SettingsAccount> findAll(String accountId, String serviceName);

    SettingsAccount save(SettingsAccount entity);

    void delete(SettingsAccount settingsAccount);
}
