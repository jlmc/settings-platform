package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo.repositories;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo.documents.ServiceJsonSchemasDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceJsonSchemasDocumentRepository extends MongoRepository<ServiceJsonSchemasDocument, String> {
}
