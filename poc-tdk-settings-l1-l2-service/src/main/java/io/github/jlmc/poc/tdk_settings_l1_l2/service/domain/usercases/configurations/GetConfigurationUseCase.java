package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.configurations;

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
import java.util.Optional;
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

    private static List<Supplier<ObjectNode>> asSupplier(List<SettingsAccount> settings) {
        return settings.stream().map(account -> (Supplier<ObjectNode>) account).toList();
    }

    public Map<String, Object> execute(Input input) {
        List<SettingsAccount> settings = fetchSettings(input);
        List<Supplier<ObjectNode>> suppliers = asSupplier(settings);
        Map<String, Object> configurations = objectNodeMerger.mergeContentsAsMap(suppliers);

        List<SettingsAccount> decryptedSettings = decryptSettingsAccounts(input, settings);

        Map<String, Object> result;
        if (decryptedSettings != settings) {
            result = objectNodeMerger.mergeContentsAsMap(asSupplier(decryptedSettings));
        } else {
            result = configurations;
        }

        eventPublisher.publishEvent(new ConfigurationHitEvent(
                input.accountId(),
                input.serviceName(),
                input.configurationType(),
                settings,
                configurations
        ));

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

    private List<SettingsAccount> decryptSettingsAccounts(Input input, List<SettingsAccount> settings) {
        String serviceName = input.serviceName();

        if (!input.hasPrivateKey()) {
            log.debug("No private key provided, skipping decryption for account={}, serviceId={}", input.accountId(), serviceName);
            return settings;
        }

        Optional<ServiceJsonSchemas> serviceJsonSchemasOpt = serviceJsonSchemasRepository.findByServiceName(serviceName);
        if (serviceJsonSchemasOpt.isEmpty()) {
            log.debug("No Service JSON schemas found for serviceId={}, skipping decryption", serviceName);
            return settings;
        }

        log.debug("Decrypting configuration JSONs for serviceId={}", serviceName);

        List<SettingsAccount> clonedSettings = settings.stream().map(SettingsAccount::copy).toList();

        ServiceJsonSchemas serviceJsonSchemas = serviceJsonSchemasOpt.get();
        return settingsAccountDecrypter.decryptConfigurationJsons(clonedSettings, serviceJsonSchemas, input.privateKey());
    }

}
