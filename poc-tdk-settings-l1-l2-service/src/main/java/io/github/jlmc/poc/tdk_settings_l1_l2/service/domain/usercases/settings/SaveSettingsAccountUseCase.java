package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.settings;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonSchema;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.exceptions.JsonSchemaValidatorErrorException;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.exceptions.SettingsAccountJsonValidationException;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.JsonObjectSchemaValidator;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.JsonSchemaValidator;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ServiceJsonSchemasRepository;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SettingsAccountRepository;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SharedCacheSynchronizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SaveSettingsAccountUseCase {

    private final SettingsAccountRepository repository;
    private final ServiceJsonSchemasRepository serviceJsonSchemasRepository;
    private final JsonObjectSchemaValidator validator;
    private final SharedCacheSynchronizer sharedCacheSynchronizer;


    public SaveSettingsAccountUseCase(SettingsAccountRepository repository,
                                      ServiceJsonSchemasRepository serviceJsonSchemasRepository,
                                      JsonObjectSchemaValidator validator,
                                      SharedCacheSynchronizer sharedCacheSynchronizer) {
        this.repository = repository;
        this.serviceJsonSchemasRepository = serviceJsonSchemasRepository;
        this.validator = validator;
        this.sharedCacheSynchronizer = sharedCacheSynchronizer;
    }

    @Transactional
    public SettingsAccount execute(SettingsAccount entity) {
        validate(entity);

        var persisted = repository.save(entity);

        sharedCacheSynchronizer.update(persisted);

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
