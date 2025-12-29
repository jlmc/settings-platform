package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.settings;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SettingsAccountRepository;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SharedCacheSynchronizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DeleteSettingsAccountUseCase {

    private final SettingsAccountRepository repository;
    private final SharedCacheSynchronizer sharedCacheSynchronizer;

    public DeleteSettingsAccountUseCase(SettingsAccountRepository repository,
                                        SharedCacheSynchronizer sharedCacheSynchronizer) {
        this.repository = repository;
        this.sharedCacheSynchronizer = sharedCacheSynchronizer;
    }

    @Transactional
    public void execute(Input input) {
        SettingsAccount settingsAccount = repository.find(input.accountId(), input.serviceName(), input.type()).orElse(null);

        if (settingsAccount != null) {
            repository.delete(settingsAccount);
            sharedCacheSynchronizer.delete(settingsAccount);
        }
    }
}
