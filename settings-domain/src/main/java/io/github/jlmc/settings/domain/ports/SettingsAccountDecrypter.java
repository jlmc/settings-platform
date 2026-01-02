package io.github.jlmc.settings.domain.ports;


import io.github.jlmc.settings.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.settings.domain.entities.SettingsAccount;

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
