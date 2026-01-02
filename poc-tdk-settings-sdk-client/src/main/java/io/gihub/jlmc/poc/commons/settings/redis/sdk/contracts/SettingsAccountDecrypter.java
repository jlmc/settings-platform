package io.gihub.jlmc.poc.commons.settings.redis.sdk.contracts;


import io.gihub.jlmc.poc.commons.settings.redis.sdk.model.ServiceJsonSchemas;
import io.gihub.jlmc.poc.commons.settings.redis.sdk.model.SettingsAccount;

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
