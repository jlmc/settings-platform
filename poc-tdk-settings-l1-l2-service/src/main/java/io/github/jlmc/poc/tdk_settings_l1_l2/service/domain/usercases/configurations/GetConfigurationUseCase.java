package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.configurations;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ResolvedConfiguration;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.ConfigurationHitEvent;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.exceptions.NotFoundException;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ObjectNodeMerger;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ServiceJsonSchemasRepository;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SettingsAccountDecrypter;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SettingsAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
public class GetConfigurationUseCase {

    private final SettingsAccountRepository settingsAccountRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectNodeMerger objectNodeMerger;
    private final ServiceJsonSchemasRepository serviceJsonSchemasRepository;
    private final SettingsAccountDecrypter settingsAccountDecrypter;

    public GetConfigurationUseCase(SettingsAccountRepository settingsAccountRepository,
                                   ApplicationEventPublisher eventPublisher,
                                   ObjectNodeMerger objectNodeMerger,
                                   ServiceJsonSchemasRepository serviceJsonSchemasRepository,
                                   SettingsAccountDecrypter settingsAccountDecrypter) {
        this.settingsAccountRepository = settingsAccountRepository;
        this.eventPublisher = eventPublisher;
        this.objectNodeMerger = objectNodeMerger;
        this.serviceJsonSchemasRepository = serviceJsonSchemasRepository;
        this.settingsAccountDecrypter = settingsAccountDecrypter;
    }

    private static List<Supplier<ObjectNode>> asSuppliers(List<SettingsAccount> settings) {
        return settings.stream().map(account -> (Supplier<ObjectNode>) account).toList();
    }

    public Map<String, Object> execute(Input input) {
        List<SettingsAccount> maybeEncryptedSettings = fetchSettings(input);

        List<Supplier<ObjectNode>> suppliers = asSuppliers(maybeEncryptedSettings);
        Map<String, Object> maybeEncryptedSettingsResults = objectNodeMerger.mergeContentsAsMap(suppliers);

        ServiceJsonSchemas serviceJsonSchemas = serviceJsonSchemasRepository.findByServiceName(input.serviceName()).orElse(null);
        List<SettingsAccount> decryptedSettings = decryptSettingsAccounts(input, maybeEncryptedSettings, serviceJsonSchemas);

        Map<String, Object> result;
        if (decryptedSettings != maybeEncryptedSettings) {
            result = objectNodeMerger.mergeContentsAsMap(asSuppliers(decryptedSettings));
        } else {
            result = maybeEncryptedSettingsResults;
        }

        ResolvedConfiguration resolvedConfiguration = new ResolvedConfiguration(
                input.accountId(),
                input.serviceName(),
                input.configurationType(),
                serviceJsonSchemas,
                maybeEncryptedSettings,
                maybeEncryptedSettingsResults
        );

        eventPublisher.publishEvent(new ConfigurationHitEvent(resolvedConfiguration));

        return result;
    }

    private List<SettingsAccount> fetchSettings(Input input) {
        List<SettingsAccount> settings = settingsAccountRepository.findAll(input.accountId(), input.serviceName());

        List<SettingsAccount> filteredSettings = settings.stream()
                .filter(SettingsAccount.settingsAccountWithinPriorityThreshold(input.configurationType()))
                .sorted(SettingsAccount.BY_PRIORITY)
                .toList();

        if (filteredSettings.isEmpty()) {
            throw new NotFoundException(
                    "No Configurations found for accountId=%s, serviceName=%s, configurationType=%s"
                            .formatted(input.accountId(), input.serviceName(), input.configurationType())
            );
        }

        return filteredSettings;
    }

    private List<SettingsAccount> decryptSettingsAccounts(Input input, List<SettingsAccount> settings, ServiceJsonSchemas serviceJsonSchemas) {
        String serviceName = input.serviceName();

        if (!input.hasPrivateKey()) {
            log.debug("No private key provided, skipping decryption for account={}, serviceId={}", input.accountId(), serviceName);
            return settings;
        }

        if (serviceJsonSchemas == null) {
            log.debug("No Service JSON schemas found for serviceId={}, skipping decryption", serviceName);
            return settings;
        }

        log.debug("Decrypting configuration JSONs for serviceId={}", serviceName);

        List<SettingsAccount> clonedSettings = settings.stream().map(SettingsAccount::copy).toList();

        return settingsAccountDecrypter.decryptConfigurationJsons(clonedSettings, serviceJsonSchemas, input.privateKey());
    }

}
