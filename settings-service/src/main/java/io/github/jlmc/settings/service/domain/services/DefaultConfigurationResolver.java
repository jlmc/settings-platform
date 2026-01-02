package io.github.jlmc.settings.service.domain.services;

import io.github.jlmc.settings.service.domain.entities.ResolvedConfiguration;
import io.github.jlmc.settings.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.settings.service.domain.entities.SettingsAccount;
import io.github.jlmc.settings.service.domain.events.ConfigurationHitEvent;
import io.github.jlmc.settings.service.domain.exceptions.NotFoundException;
import io.github.jlmc.settings.service.domain.inputs.ResolveConfigurationInput;
import io.github.jlmc.settings.service.domain.ports.ObjectNodeMerger;
import io.github.jlmc.settings.service.domain.ports.ServiceJsonSchemasRepository;
import io.github.jlmc.settings.service.domain.ports.SettingsAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static io.github.jlmc.settings.service.domain.entities.SettingsAccount.asSuppliers;

@Slf4j
@Component
public class DefaultConfigurationResolver implements ConfigurationResolver {

    private final SettingsAccountRepository settingsAccountRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectNodeMerger objectNodeMerger;
    private final ServiceJsonSchemasRepository serviceJsonSchemasRepository;

    public DefaultConfigurationResolver(SettingsAccountRepository settingsAccountRepository,
                                        ApplicationEventPublisher eventPublisher,
                                        ObjectNodeMerger objectNodeMerger,
                                        ServiceJsonSchemasRepository serviceJsonSchemasRepository) {
        this.settingsAccountRepository = settingsAccountRepository;
        this.eventPublisher = eventPublisher;
        this.objectNodeMerger = objectNodeMerger;
        this.serviceJsonSchemasRepository = serviceJsonSchemasRepository;
    }

    @Override
    public ResolvedConfiguration resolve(ResolveConfigurationInput input) {
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

    private List<SettingsAccount> fetchSettings(ResolveConfigurationInput input) {
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

    private ServiceJsonSchemas loadSchemas(String serviceName) {
        return serviceJsonSchemasRepository.findByServiceName(serviceName).orElse(null);
    }

    private Map<String, Object> mergeSettings(List<SettingsAccount> settings) {
        return objectNodeMerger.mergeContentsAsMap(asSuppliers(settings));
    }

}
