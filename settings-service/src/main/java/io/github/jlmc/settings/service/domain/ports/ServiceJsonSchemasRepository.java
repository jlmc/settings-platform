package io.github.jlmc.settings.service.domain.ports;

import io.github.jlmc.settings.service.domain.entities.ServiceJsonSchemas;

import java.util.Optional;

public interface ServiceJsonSchemasRepository {

    ServiceJsonSchemas save(ServiceJsonSchemas serviceJsonSchemas);

    void delete(String serviceName);

    Optional<ServiceJsonSchemas> findByServiceName(String serviceName);
}
