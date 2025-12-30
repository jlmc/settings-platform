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

        ResolvedConfiguration resolvedConfiguration = resolveConfigurationAndPublishEvent(input);

        if (!input.hasPrivateKey()) {
            return resolvedConfiguration.mergedSettings();
        }

        return decryptForReturn(
                input,
                resolvedConfiguration.settingsAccounts(),
                resolvedConfiguration.serviceJsonSchemas()
        );
    }

    private Map<String, Object> decryptForReturn(Input input, List<SettingsAccount> originalSettings, ServiceJsonSchemas schemas) {
        if (schemas == null) {
            log.debug(
                    "No Service JSON schemas found for serviceId={}, returning encrypted values",
                    input.serviceName()
            );
            return mergeSettings(originalSettings);
        }

        List<SettingsAccount> clonedSettings = originalSettings.stream()
                .map(SettingsAccount::copy)
                .toList();

        List<SettingsAccount> decryptedSettings =
                settingsAccountDecrypter.decryptConfigurationJsons(
                        clonedSettings,
                        schemas,
                        input.privateKey()
                );

        return mergeSettings(decryptedSettings);
    }

    private ResolvedConfiguration resolveConfigurationAndPublishEvent(Input input) {
        List<SettingsAccount> originalSettings = fetchSettings(input);
        ServiceJsonSchemas schemas = loadSchemas(input.serviceName());
        Map<String, Object> originalMergedConfiguration = mergeSettings(originalSettings);

        ResolvedConfiguration resolved = new ResolvedConfiguration(
                input.accountId(),
                input.serviceName(),
                input.configurationType(),
                schemas,
                originalSettings,
                originalMergedConfiguration
        );

        publishHitEvent(resolved);

        return resolved;
    }

    private void publishHitEvent(ResolvedConfiguration resolved) {
        eventPublisher.publishEvent(new ConfigurationHitEvent(resolved));
    }

    private Map<String, Object> mergeSettings(List<SettingsAccount> settings) {
        return objectNodeMerger.mergeContentsAsMap(asSuppliers(settings));
    }

    private ServiceJsonSchemas loadSchemas(String serviceName) {
        return serviceJsonSchemasRepository.findByServiceName(serviceName).orElse(null);
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
}
