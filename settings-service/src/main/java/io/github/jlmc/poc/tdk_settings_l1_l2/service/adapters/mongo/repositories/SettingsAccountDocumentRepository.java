package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo.repositories;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo.documents.SettingsAccountDocument;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SettingsAccountDocumentRepository extends MongoRepository<SettingsAccountDocument, String> {
    Optional<SettingsAccountDocument> findByAccountIdAndServiceNameAndType(@NotBlank String accountId, @NotBlank String serviceName, @NotNull @NotNull ConfigurationType type);

    List<SettingsAccountDocument> findByAccountIdAndServiceName(@NotBlank String accountId, @NotBlank String serviceName);
}
