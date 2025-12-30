package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.settings;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonSchema;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.SettingsAccountUpdatedEvent;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.exceptions.SettingsAccountJsonValidationException;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.JsonObjectSchemaValidator;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ServiceJsonSchemasRepository;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SettingsAccountRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SaveSettingsAccountUseCase {

    private final SettingsAccountRepository settingsAccountRepository;
    private final ServiceJsonSchemasRepository serviceJsonSchemasRepository;
    private final JsonObjectSchemaValidator validator;
    private final ApplicationEventPublisher eventPublisher;


    public SaveSettingsAccountUseCase(SettingsAccountRepository settingsAccountRepository,
                                      ServiceJsonSchemasRepository serviceJsonSchemasRepository,
                                      JsonObjectSchemaValidator validator,
                                      ApplicationEventPublisher eventPublisher) {
        this.settingsAccountRepository = settingsAccountRepository;
        this.serviceJsonSchemasRepository = serviceJsonSchemasRepository;
        this.validator = validator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SettingsAccount execute(SettingsAccount entity) {
        validate(entity);

        var persisted = settingsAccountRepository.save(entity);

        eventPublisher.publishEvent(new SettingsAccountUpdatedEvent(persisted));

        return persisted;
    }

    private void validate(SettingsAccount entity) {
        Optional<ServiceJsonSchemas> serviceJsonSchemasOpt = serviceJsonSchemasRepository.findByServiceName(entity.serviceName());
        if (serviceJsonSchemasOpt.isEmpty()) {
            return;
        }

        ServiceJsonSchemas serviceJsonSchemas = serviceJsonSchemasOpt.get();


        Optional<JsonSchema> byConfigurationType = serviceJsonSchemas.findByConfigurationType(entity.type());

        if (byConfigurationType.isEmpty()) {
            return;
        }

        var validationResult = validator.validate(entity, byConfigurationType.get());

        if(!validationResult.valid()) {
            throw new SettingsAccountJsonValidationException(
                    "SettingsAccount JSON validation errors for accountId=" + entity.accountId() +
                            ", serviceName=" + entity.serviceName() +
                            ", type=" + entity.type(),
                    validationResult.errors()
            );
        }
    }
}
