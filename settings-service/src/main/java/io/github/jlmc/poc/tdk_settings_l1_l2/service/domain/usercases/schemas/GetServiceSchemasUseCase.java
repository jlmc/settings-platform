package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.schemas;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.exceptions.NotFoundException;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ServiceJsonSchemasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetServiceSchemasUseCase {

    private final ServiceJsonSchemasRepository repository;

    public GetServiceSchemasUseCase(ServiceJsonSchemasRepository repository) {
        this.repository = repository;
    }

    public ServiceJsonSchemas execute(String serviceName) {
        return repository.findByServiceName(serviceName)
                .orElseThrow(() -> new NotFoundException("Service JSON schemas not found for serviceId=" + serviceName));
    }

}
