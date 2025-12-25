package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo.documents.ServiceJsonSchemasDocument;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo.repositories.ServiceJsonSchemasDocumentRepository;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ServiceJsonSchemasRepository;
import lombok.val;
import org.springframework.stereotype.Repository;

@Repository
public class MongoServiceJsonSchemasRepository implements ServiceJsonSchemasRepository {

    private final ServiceJsonSchemasDocumentRepository documentRepository;

    public MongoServiceJsonSchemasRepository(ServiceJsonSchemasDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public ServiceJsonSchemas save(ServiceJsonSchemas serviceJsonSchemas) {
        var document = documentRepository.save(ServiceJsonSchemasDocument.from(serviceJsonSchemas));
        return document.toEntity();
    }
}
