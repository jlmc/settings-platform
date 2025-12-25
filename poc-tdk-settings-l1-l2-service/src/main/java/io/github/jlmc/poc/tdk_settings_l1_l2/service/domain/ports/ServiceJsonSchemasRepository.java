package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo.documents.ServiceJsonSchemasDocument;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;

public interface ServiceJsonSchemasRepository {

    ServiceJsonSchemasDocument save(ServiceJsonSchemas serviceJsonSchemas);

}
