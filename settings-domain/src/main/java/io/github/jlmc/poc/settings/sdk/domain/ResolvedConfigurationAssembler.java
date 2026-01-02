package io.github.jlmc.poc.settings.sdk.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jlmc.poc.settings.sdk.domain.components.JacksonObjectNodeMerger;
import io.github.jlmc.poc.settings.sdk.domain.components.RSADecryptor;
import io.github.jlmc.poc.settings.sdk.domain.components.SettingsAccountJsonDecrypter;
import io.github.jlmc.poc.settings.sdk.domain.entities.ResolvedConfiguration;
import io.github.jlmc.poc.settings.sdk.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.settings.sdk.domain.entities.SettingsAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static io.github.jlmc.poc.settings.sdk.domain.entities.SettingsAccount.asSuppliers;

public class ResolvedConfigurationAssembler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolvedConfigurationAssembler.class);

    private final ObjectNodeMerger objectNodeMerger;
    private final SettingsAccountDecrypter settingsAccountDecrypter;

    ResolvedConfigurationAssembler(ObjectNodeMerger objectNodeMerger, SettingsAccountDecrypter settingsAccountDecrypter) {
        this.objectNodeMerger = objectNodeMerger;
        this.settingsAccountDecrypter = settingsAccountDecrypter;
    }

    public static ResolvedConfigurationAssembler defaultAssembler(ObjectMapper objectMapper) {
        LOGGER.debug("Creating default ResolvedConfigurationAssembler");

        ObjectNodeMerger objectNodeMerger = new JacksonObjectNodeMerger(objectMapper);
        SettingsAccountDecrypter settingsAccountDecrypter = new SettingsAccountJsonDecrypter(new RSADecryptor());
        return new ResolvedConfigurationAssembler(objectNodeMerger, settingsAccountDecrypter);
    }

    private static boolean hasPrivateKey(String privateKey) {
        return privateKey == null || privateKey.isBlank();
    }

    public Map<String, Object> assemble(ResolvedConfiguration input, String privateKey) {

        boolean hasPrivateKey = hasPrivateKey(privateKey);
        boolean hasSchemas = input.hasSchema();

        if (!hasPrivateKey || !hasSchemas) {
            LOGGER.debug(
                    "Skipping decryption. Reason: privateKeyPresent={}, serviceJsonSchemasPresent={}",
                    hasPrivateKey,
                    hasSchemas
            );

            return objectNodeMerger.mergeContentsAsMap(asSuppliers(input.settingsAccounts()));
        }

        LOGGER.debug("Decrypting {} settings account(s) using service schemas", input.settingsAccounts().size());

        ServiceJsonSchemas serviceJsonSchemas = input.serviceJsonSchemas();
        List<SettingsAccount> decryptedOriginalSettings = settingsAccountDecrypter.decryptConfigurationJsons(input.settingsAccounts(), serviceJsonSchemas, privateKey);

        LOGGER.debug("Decryption completed. Merging decrypted configuration contents");

        return objectNodeMerger.mergeContentsAsMap(asSuppliers(decryptedOriginalSettings));
    }
}
