package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.crypto;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonSchema;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.Rsa;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.Decryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsAccountJsonDecrypterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Decryptor decryptor;

    @InjectMocks
    private SettingsAccountJsonDecrypter victim;

    private List<SettingsAccount> configurations;
    private String privateKey;

    @BeforeEach
    void setUp() {
        privateKey = "private-key";
        ObjectNode content = objectMapper.createObjectNode()
                .put("username", "john")
                .put("password", "encrypted-password");

        configurations = List.of(
                new SettingsAccount(ConfigurationType.ACCOUNT, "acc-1", "service-1", content)
        );
    }

    @Test
    void decryptConfigurationJsonsWithNullSchema() {
        List<SettingsAccount> result = victim.decryptConfigurationJsons(configurations, null, privateKey);

        assertSame(configurations, result);
        verifyNoInteractions(decryptor);
    }

    @Test
    void decryptConfigurationJsonsWithNullPrivateKey() {
        ServiceJsonSchemas schema = createSchema(ConfigurationType.ACCOUNT, createFieldSchema(true));

        List<SettingsAccount> result = victim.decryptConfigurationJsons(configurations, schema, null);

        assertSame(configurations, result);
        verifyNoInteractions(decryptor);
    }

    @Test
    void decryptConfigurationJsonsWithEmptyPrivateKey() {
        ServiceJsonSchemas schema = createSchema(ConfigurationType.ACCOUNT, createFieldSchema(true));

        List<SettingsAccount> result = victim.decryptConfigurationJsons(configurations, schema, "");

        assertSame(configurations, result);
        verifyNoInteractions(decryptor);
    }

    @Test
    void decryptConfigurationJsonsWithMissingRsaInSchema() {
        ServiceJsonSchemas schema = new ServiceJsonSchemas("service-1", List.of(new JsonSchema(ConfigurationType.ACCOUNT, createFieldSchema(true))), null);

        List<SettingsAccount> result = victim.decryptConfigurationJsons(configurations, schema, privateKey);

        assertSame(configurations, result);
        verifyNoInteractions(decryptor);
    }

    @Test
    void decryptConfigurationJsonsWithMatchingSchemaAndEncryptedField() {
        ServiceJsonSchemas schema = createSchema(ConfigurationType.ACCOUNT, createFieldSchema(true));
        when(decryptor.decrypt("encrypted-password", privateKey)).thenReturn("decrypted-password");

        List<SettingsAccount> result = victim.decryptConfigurationJsons(configurations, schema, privateKey);

        assertEquals(1, result.size());
        assertEquals("decrypted-password", result.getFirst().content().get("password").asString());
        assertEquals("john", result.getFirst().content().get("username").asString());
        verify(decryptor).decrypt("encrypted-password", privateKey);
    }

    @Test
    void decryptConfigurationJsonsWithNonMatchingConfigurationType() {
        ServiceJsonSchemas schema = createSchema(ConfigurationType.SERVICE, createFieldSchema(true));

        List<SettingsAccount> result = victim.decryptConfigurationJsons(configurations, schema, privateKey);

        assertEquals(1, result.size());
        assertEquals("encrypted-password", result.getFirst().content().get("password").asString());
        verifyNoInteractions(decryptor);
    }

    @Test
    void decryptFieldsWithNestedObject() {
        ObjectNode schemaContent = objectMapper.createObjectNode();
        ObjectNode properties = schemaContent.putObject("properties");
        
        ObjectNode nestedSchema = properties.putObject("nested");
        nestedSchema.put("type", "object");
        ObjectNode nestedProperties = nestedSchema.putObject("properties");
        
        ObjectNode secretSchema = nestedProperties.putObject("secret");
        secretSchema.put("type", "string");
        secretSchema.put("encrypt", true);

        ObjectNode payload = objectMapper.createObjectNode();
        ObjectNode nestedPayload = payload.putObject("nested");
        nestedPayload.put("secret", "encrypted-secret");

        when(decryptor.decrypt("encrypted-secret", privateKey)).thenReturn("decrypted-secret");

        ObjectNode result = victim.decryptFields(payload, schemaContent, privateKey);

        assertEquals("decrypted-secret", result.get("nested").get("secret").asString());
    }

    @Test
    void decryptFieldsWhenFieldMissingInPayloadButPresentInSchema() {
        ObjectNode schemaContent = createFieldSchema(true);
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("other", "value");

        ObjectNode result = victim.decryptFields(payload, schemaContent, privateKey);

        assertEquals(1, result.size());
        assertEquals("value", result.get("other").asString());
        verifyNoInteractions(decryptor);
    }

    @Test
    void decryptFieldsWhenFieldPresentInPayloadButMissingInSchema() {
        ObjectNode schemaContent = objectMapper.createObjectNode();
        schemaContent.putObject("properties").putObject("someField").put("type", "string");

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("unknownField", "encrypted-maybe");

        ObjectNode result = victim.decryptFields(payload, schemaContent, privateKey);

        assertEquals("encrypted-maybe", result.get("unknownField").asString());
        verifyNoInteractions(decryptor);
    }

    @Test
    void decryptFieldsWithNonStringEncryptedField() {
        ObjectNode schemaContent = createFieldSchema(true);
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("password", 12345);

        ObjectNode result = victim.decryptFields(payload, schemaContent, privateKey);

        assertEquals(12345, result.get("password").asInt());
        verifyNoInteractions(decryptor);
    }

    private ServiceJsonSchemas createSchema(ConfigurationType type, ObjectNode schemaContent) {
        return new ServiceJsonSchemas("service-1", List.of(new JsonSchema(type, schemaContent)), new Rsa("public-key"));
    }

    private ObjectNode createFieldSchema(boolean encrypt) {
        ObjectNode schema = objectMapper.createObjectNode();
        ObjectNode properties = schema.putObject("properties");
        ObjectNode passwordField = properties.putObject("password");
        passwordField.put("encrypt", encrypt);
        passwordField.put("type", "string");
        return schema;
    }
}
