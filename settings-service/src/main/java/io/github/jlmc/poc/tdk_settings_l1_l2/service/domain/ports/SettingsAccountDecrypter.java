package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;

import java.util.List;

public interface SettingsAccountDecrypter {
    /**
     * Decrypts all configuration JSONs based on the schema and RSA private key.
     */
    List<SettingsAccount> decryptConfigurationJsons(
            List<SettingsAccount> configurations,
            ServiceJsonSchemas schema,
            String rsaPrivateKey
    );
}
