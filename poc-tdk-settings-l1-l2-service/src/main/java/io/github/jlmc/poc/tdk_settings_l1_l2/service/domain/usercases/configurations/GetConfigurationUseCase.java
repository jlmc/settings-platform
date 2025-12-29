package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.configurations;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.exceptions.NotFoundException;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ObjectNodeMerger;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SettingsAccountRepository;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SharedCacheSynchronizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
public class GetConfigurationUseCase {

    private final SettingsAccountRepository settingsAccountRepository;
    private final SharedCacheSynchronizer sharedCacheSynchronizer;
    private final ObjectNodeMerger objectNodeMerger;

    public GetConfigurationUseCase(SettingsAccountRepository settingsAccountRepository,
                                   SharedCacheSynchronizer sharedCacheSynchronizer,
                                   ObjectNodeMerger objectNodeMerger) {
        this.settingsAccountRepository = settingsAccountRepository;
        this.sharedCacheSynchronizer = sharedCacheSynchronizer;
        this.objectNodeMerger = objectNodeMerger;
    }

    public Map<String, Object> execute(String accountId, String serviceName, ConfigurationType configurationType) {
        List<SettingsAccount> settings = getAllSettings(accountId, serviceName, configurationType);


        Map<String, Object> configurations = objectNodeMerger.mergeContentsAsMap(asSupplier(settings));

        sharedCacheSynchronizer.hit(new SharedCacheSynchronizer.HitData(
                accountId,
                serviceName,
                configurationType,
                settings,
                configurations
        ));

        return configurations;
    }

    private List<SettingsAccount> getAllSettings(String accountId, String serviceName, ConfigurationType configurationType) {
        List<SettingsAccount> settings = settingsAccountRepository.findAll(accountId, serviceName);

        if (settings.isEmpty()) {
            throw new NotFoundException(
                    "No Configurations found for accountId=" + accountId + ", serviceName=" + serviceName + ", configurationType=" + configurationType
            );
        }

        return settings.stream()
                .filter(SettingsAccount.settingsAccountWithinPriorityThreshold(configurationType))
                .sorted(SettingsAccount.BY_PRIORITY)
                //.map(account -> (Supplier<ObjectNode>) account)
                .toList();
    }

    private static List<Supplier<ObjectNode>> asSupplier(List<SettingsAccount> settings) {
        return settings.stream().map(account -> (Supplier<ObjectNode>) account).toList();
    }

}
