package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.crypto;


import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonSchema;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.Decryptor;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SettingsAccountDecrypter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SettingsAccountJsonDecrypter implements SettingsAccountDecrypter {
    private static final String PROPERTIES = "properties";
    private static final String ENCRYPT = "encrypt";
    private static final String TYPE = "type";
    private static final String OBJECT = "object";

    private final Decryptor decryptor;

    public SettingsAccountJsonDecrypter(Decryptor decryptor) {
        this.decryptor = decryptor;
    }

    /**
     * Decrypts all configuration JSONs based on the schema and RSA private key.
     */
    public List<SettingsAccount> decryptConfigurationJsons(
            List<SettingsAccount> configurations,
            ServiceJsonSchemas schema,
            String rsaPrivateKey
    ) {
        if (schema == null || rsaPrivateKey == null || rsaPrivateKey.isEmpty()) {
            return configurations;
        }

        if (schema.rsa() == null || schema.rsa().publicKey() == null || schema.rsa().publicKey().isBlank()) {
            return configurations;
        }

        Map<ConfigurationType, JsonNode> jsonSchemas = schema.schemas().stream()
                .collect(Collectors.toMap(
                        JsonSchema::schemaType,
                        JsonSchema::schemaContent,
                        (existing, replacement) -> existing
                ));

        return configurations.stream()
                .map(config -> {

                    JsonNode jsonSchema = jsonSchemas.get(config.type());

                    if (jsonSchema != null && config.content() != null && config.content().isObject()) {
                        // Make a copy of the original JSON
                        ObjectNode copy = config.content().deepCopy();

                        // Decrypt fields on the copy
                        ObjectNode decryptedValue = decryptFields(copy, jsonSchema, rsaPrivateKey);


                        return config.copyWithJson(decryptedValue);
                    } else {
                        return config;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Recursively decrypts fields marked as encrypted in the JSON schema.
     */
    public ObjectNode decryptFields(ObjectNode valueNode, JsonNode jsonSchema, String privateKey) {
        // Create a deep copy so the original valueNode is not mutated

        JsonNode propertiesNode = jsonSchema.get(PROPERTIES);
        if (propertiesNode == null || !propertiesNode.isObject()) {
            return valueNode;
        }

        Set<Map.Entry<String, JsonNode>> properties = propertiesNode.properties();

        for (Map.Entry<String, JsonNode> entry : properties) {
            String fieldName = entry.getKey();
            JsonNode fieldSchema = entry.getValue();
            JsonNode fieldValue = valueNode.get(fieldName);

            if (fieldValue != null && fieldValue.isString() && fieldSchema.get(ENCRYPT) != null
                    && fieldSchema.get(ENCRYPT).asBoolean(false)) {
                String decryptedText = rsaDecrypt(fieldValue.asString(), privateKey);
                valueNode.put(fieldName, decryptedText);
            } else if (fieldValue != null && fieldValue.isObject() && fieldSchema.get(TYPE) != null
                    && OBJECT.equals(fieldSchema.get(TYPE).asString())) {
                decryptFields((ObjectNode) fieldValue, fieldSchema, privateKey);
            }
        }

        return valueNode;
    }

    /**
     * Delegates RSA decryption to RSADecryptor.
     */
    public String rsaDecrypt(String text, String privateKey) {
        return decryptor.decrypt(text, privateKey);
    }
}
