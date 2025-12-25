package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo.documents.ServiceJsonSchemasDocument;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo.repositories.ServiceJsonSchemasDocumentRepository;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ServiceJsonSchemasRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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

    @Override
    public void delete(String serviceName) {
        documentRepository.deleteById(serviceName);
    }

    @Override
    public Optional<ServiceJsonSchemas> findByServiceName(String serviceName) {
        return documentRepository.findById(serviceName.toLowerCase())
                .map(ServiceJsonSchemasDocument::toEntity);
    }
}
