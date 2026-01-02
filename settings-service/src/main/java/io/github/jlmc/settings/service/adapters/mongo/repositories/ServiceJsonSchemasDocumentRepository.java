package io.github.jlmc.settings.service.adapters.mongo.repositories;

import io.github.jlmc.settings.service.adapters.mongo.documents.ServiceJsonSchemasDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceJsonSchemasDocumentRepository extends MongoRepository<ServiceJsonSchemasDocument, String> {
}
