package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo.documents.ServiceJsonSchemasDocument;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ServiceJsonSchemasRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PersistSchemaUseCase {

    private final ServiceJsonSchemasRepository serviceJsonSchemasRepository;

    public PersistSchemaUseCase(ServiceJsonSchemasRepository serviceJsonSchemasRepository) {
        this.serviceJsonSchemasRepository = serviceJsonSchemasRepository;
    }

    @Transactional
    public void execute(ServiceJsonSchemas entity) {
        log.info("Persisting service JSON schemas for serviceId={}", entity.serviceName());
        serviceJsonSchemasRepository.save(entity);


    }

}
