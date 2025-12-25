package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;

import java.util.Optional;

public interface ServiceJsonSchemasRepository {

    ServiceJsonSchemas save(ServiceJsonSchemas serviceJsonSchemas);

    void delete(String serviceName);

    Optional<ServiceJsonSchemas> findByServiceName(String serviceName);
}
