package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonValidationResult;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.exceptions.JsonSchemaValidatorErrorException;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.JsonSchemaValidator;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ServiceJsonSchemasRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PersistSchemaUseCase {

    private final ServiceJsonSchemasRepository serviceJsonSchemasRepository;
    private final JsonSchemaValidator jsonSchemaValidator;

    public PersistSchemaUseCase(ServiceJsonSchemasRepository serviceJsonSchemasRepository, JsonSchemaValidator jsonSchemaValidator) {
        this.serviceJsonSchemasRepository = serviceJsonSchemasRepository;
        this.jsonSchemaValidator = jsonSchemaValidator;
    }

    @Transactional
    public ServiceJsonSchemas execute(ServiceJsonSchemas entity) {
        log.info("Persisting service JSON schemas for serviceId={}", entity.serviceName());

        Map<String, List<JsonValidationResult>> invalidSchemas = entity
                .schemas()
                .stream()
                .map(schema -> new Pair<>(schema.schemaType(), jsonSchemaValidator.validate(schema)))
                .filter(pairs -> !pairs.second().valid())
                .collect(Collectors.groupingBy(
                        Pair::first,
                        Collectors.mapping(Pair::second, Collectors.toList())
                ));

        if (!invalidSchemas.isEmpty()) {
            log.debug("Validation errors found for serviceId={}: {}", entity.serviceName(), invalidSchemas);
            throw new JsonSchemaValidatorErrorException("Validation errors found for serviceId=" + entity.serviceName(), invalidSchemas);
        }

        return serviceJsonSchemasRepository.save(entity);
    }

    private record Pair<T, U>(T first, U second) {
    }
}
