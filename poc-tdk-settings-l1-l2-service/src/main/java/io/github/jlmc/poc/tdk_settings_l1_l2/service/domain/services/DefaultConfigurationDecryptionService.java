package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.services;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ResolvedConfiguration;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ObjectNodeMerger;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SettingsAccountDecrypter;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.configurations.Input;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount.asSuppliers;

@Slf4j
@Component
public class DefaultConfigurationDecryptionService implements ConfigurationDecryptionService {

    private final SettingsAccountDecrypter settingsAccountDecrypter;
    private final ObjectNodeMerger objectNodeMerger;


    public DefaultConfigurationDecryptionService(SettingsAccountDecrypter settingsAccountDecrypter, ObjectNodeMerger objectNodeMerger) {
        this.settingsAccountDecrypter = settingsAccountDecrypter;
        this.objectNodeMerger = objectNodeMerger;
    }

    @Override
    public Map<String, Object> decryptForReturn(Input input, ResolvedConfiguration resolved) {
        if (!input.hasPrivateKey()) {
            return resolved.mergedSettings();
        }

        return decryptForReturn(
                input,
                resolved.settingsAccounts(),
                resolved.serviceJsonSchemas()
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

    private Map<String, Object> mergeSettings(List<SettingsAccount> settings) {
        return objectNodeMerger.mergeContentsAsMap(asSuppliers(settings));
    }

}
