package io.github.jlmc.settings.service.domain.usercases.schemas;

import io.github.jlmc.settings.service.domain.ports.ServiceJsonSchemasRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class DeleteSchemaUseCase {

    private final ServiceJsonSchemasRepository serviceJsonSchemasRepository;

    public DeleteSchemaUseCase(ServiceJsonSchemasRepository serviceJsonSchemasRepository) {
        this.serviceJsonSchemasRepository = serviceJsonSchemasRepository;
    }

    @Transactional
    public void execute(String serviceName) {
        log.info("Deleting service JSON schemas for serviceId={}", serviceName);
        serviceJsonSchemasRepository.delete(serviceName);
    }

}
