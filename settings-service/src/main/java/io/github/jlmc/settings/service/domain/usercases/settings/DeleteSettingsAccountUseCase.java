package io.github.jlmc.settings.service.domain.usercases.settings;

import io.github.jlmc.settings.service.domain.entities.SettingsAccount;
import io.github.jlmc.settings.service.domain.events.SettingsAccountDeletedEvent;
import io.github.jlmc.settings.service.domain.inputs.SettingsInput;
import io.github.jlmc.settings.service.domain.ports.SettingsAccountRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteSettingsAccountUseCase {

    private final SettingsAccountRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public DeleteSettingsAccountUseCase(SettingsAccountRepository repository,
                                        ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void execute(SettingsInput input) {
        SettingsAccount settingsAccount = repository.find(input.accountId(), input.serviceName(), input.type()).orElse(null);

        if (settingsAccount != null) {
            repository.delete(settingsAccount);
            eventPublisher.publishEvent(new SettingsAccountDeletedEvent(settingsAccount));
        }
    }
}
