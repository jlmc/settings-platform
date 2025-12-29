package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.configurations;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SettingsAccountRepository;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SharedCacheSynchronizer;
import org.springframework.stereotype.Service;

@Service
public class GetConfigurationUseCase {

    private final SettingsAccountRepository settingsAccountRepository;
    private final SharedCacheSynchronizer sharedCacheSynchronizer;

    public GetConfigurationUseCase(SettingsAccountRepository settingsAccountRepository,
                                   SharedCacheSynchronizer sharedCacheSynchronizer) {
        this.settingsAccountRepository = settingsAccountRepository;
        this.sharedCacheSynchronizer = sharedCacheSynchronizer;
    }

    public void execute(String accountId, String serviceName, ConfigurationType type) {

    }
}
