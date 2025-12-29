package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo.documents.SettingsAccountDocument;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo.repositories.SettingsAccountDocumentRepository;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SettingsAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
public class MongoSettingsAccountRepository implements SettingsAccountRepository {

    private final SettingsAccountDocumentRepository repository;

    public MongoSettingsAccountRepository(SettingsAccountDocumentRepository repository) {
        this.repository = repository;
    }

    @Override
    public SettingsAccount save(SettingsAccount entity) {
        SettingsAccountDocument existingDocument = findDocument(entity.accountId(), entity.type(), entity.serviceName()).orElse(null);

        if (existingDocument != null) {
            log.debug("Updating existing SettingsAccount for accountId='{}', type='{}', serviceName='{}'",
                    entity.accountId(), entity.type(), entity.serviceName());


            existingDocument.updateFromEntity(entity);
            SettingsAccountDocument persistedDocument = repository.save(existingDocument);
            return persistedDocument.toEntity();
        } else {
            log.debug("Creating new SettingsAccount for accountId='{}', type='{}', serviceName='{}'",
                    entity.accountId(), entity.type(), entity.serviceName());

            SettingsAccountDocument document = SettingsAccountDocument.from(entity);
            SettingsAccountDocument persistedDocument = repository.save(document);
            return persistedDocument.toEntity();
        }

    }

    @Override
    public Optional<SettingsAccount> find(String accountId, String serviceName, ConfigurationType type) {
        return findDocument(accountId, type, serviceName)
                .map(SettingsAccountDocument::toEntity);
    }

    @Override
    public void delete(SettingsAccount settingsAccount) {
        repository.findByAccountIdAndServiceNameAndType(settingsAccount.accountId(), settingsAccount.serviceName(), settingsAccount.type())
                .ifPresent(document -> {
                    log.debug("Deleting SettingsAccount for accountId='{}', type='{}', serviceName='{}'",
                            settingsAccount.accountId(), settingsAccount.type(), settingsAccount.serviceName());
                    repository.delete(document);
                });
    }

    private Optional<SettingsAccountDocument> findDocument(String accountId, ConfigurationType type, String serviceName) {
        return repository.findByAccountIdAndServiceNameAndType(accountId, serviceName, type);
    }
}
