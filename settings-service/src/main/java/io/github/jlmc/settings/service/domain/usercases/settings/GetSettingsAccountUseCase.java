package io.github.jlmc.settings.service.domain.usercases.settings;

import io.github.jlmc.settings.service.domain.entities.SettingsAccount;
import io.github.jlmc.settings.service.domain.inputs.SettingsInput;
import io.github.jlmc.settings.service.domain.ports.SettingsAccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetSettingsAccountUseCase {

    private final SettingsAccountRepository repository;

    public GetSettingsAccountUseCase(SettingsAccountRepository repository) {
        this.repository = repository;
    }

    public Optional<SettingsAccount> execute(SettingsInput input) {
        return repository.find(input.accountId(),  input.serviceName(), input.type());
    }
}
