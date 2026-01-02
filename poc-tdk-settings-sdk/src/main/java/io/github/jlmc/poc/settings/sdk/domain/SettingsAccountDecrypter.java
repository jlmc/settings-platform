package io.github.jlmc.poc.settings.sdk.domain;


import io.github.jlmc.poc.settings.sdk.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.settings.sdk.domain.entities.SettingsAccount;

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
