package io.github.jlmc.settings.service.adapters.schemas;

import io.github.jlmc.settings.service.domain.entities.ConfigurationType;
import io.github.jlmc.settings.service.domain.entities.JsonSchema;
import io.github.jlmc.settings.service.domain.entities.JsonValidationError;
import io.github.jlmc.settings.service.domain.entities.JsonValidationResult;
import io.github.jlmc.settings.service.domain.entities.SettingsAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        DefaultJsonSchemaValidator.class,
        JacksonAutoConfiguration.class
})
class DefaultJsonSchemaValidatorTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DefaultJsonSchemaValidator victim;

    @Test
    void validateJsonNodeWithSuccess() {
        //Given
        JsonNode payload = readResourceFileContent("/schemas/valid-schema.json");

        // When
        JsonValidationResult result = victim.validate(payload);

        // Then
        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validateJsonNodeWithErrors() {
        // Given
        JsonNode payload = readResourceFileContent("/schemas/invalid-schema.json");

        // when
        JsonValidationResult result = victim.validate(payload);

        // Then
        assertFalse(result.valid());
        assertEquals(2, result.errors().size());
        Set<JsonValidationError> expectedErrors = Set.of(
                new JsonValidationError(
                        "does not have a value in the enumeration [\"array\", \"boolean\", \"integer\", \"null\", \"number\", \"object\", \"string\"]",
                        "/properties/bank_url/type",
                        null,
                        "http://json-schema.org/draft-07/schema#/definitions/simpleTypes/enum",
                        "enum"
                ),
                new JsonValidationError(
                        "integer found, array expected",
                        "/properties/bank_url/type",
                        null,
                        "http://json-schema.org/draft-07/schema#/properties/type/anyOf/1/type",
                        "type"
                )
        );
        assertEquals(expectedErrors, new HashSet<>(result.errors()));
    }

    @Test
    void validateJsonSchemaEntity() {
        // Given
        ObjectNode schemaContent = (ObjectNode) readResourceFileContent("/schemas/valid-schema.json");
        JsonSchema jsonSchema = new JsonSchema(ConfigurationType.ACCOUNT, schemaContent);

        // When
        JsonValidationResult result = victim.validate(jsonSchema);

        // Then
        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validateSettingsAccountAgainstJsonSchema() {
        // Given
        ObjectNode schemaContent = objectMapper.createObjectNode();
        schemaContent.put("$schema", "http://json-schema.org/draft-07/schema#");
        schemaContent.put("type", "object");
        ObjectNode properties = schemaContent.putObject("properties");
        properties.putObject("name").put("type", "string");
        schemaContent.putArray("required").add("name");

        JsonSchema jsonSchema = new JsonSchema(ConfigurationType.ACCOUNT, schemaContent);

        ObjectNode accountContent = objectMapper.createObjectNode();
        accountContent.put("name", "John Doe");
        SettingsAccount settingsAccount = new SettingsAccount(ConfigurationType.ACCOUNT, "acc1", "srv1", accountContent);

        // When
        JsonValidationResult result = victim.validate(settingsAccount, jsonSchema);

        // Then
        assertTrue(result.valid());
    }

    @Test
    void validateSettingsAccountAgainstJsonSchemaWithErrors() {
        // Given
        ObjectNode schemaContent = objectMapper.createObjectNode();
        schemaContent.put("$schema", "http://json-schema.org/draft-07/schema#");
        schemaContent.put("type", "object");
        ObjectNode properties = schemaContent.putObject("properties");
        properties.putObject("name").put("type", "string");
        schemaContent.putArray("required").add("name");

        JsonSchema jsonSchema = new JsonSchema(ConfigurationType.ACCOUNT, schemaContent);

        ObjectNode accountContent = objectMapper.createObjectNode();
        accountContent.put("name", 123); // Invalid type, expected string
        SettingsAccount settingsAccount = new SettingsAccount(ConfigurationType.ACCOUNT, "acc1", "srv1", accountContent);

        // When
        JsonValidationResult result = victim.validate(settingsAccount, jsonSchema);

        // Then
        assertFalse(result.valid());
        assertEquals(1, result.errors().size());
        assertEquals("integer found, string expected", result.errors().getFirst().message());
        assertEquals("/name", result.errors().getFirst().property());
    }

    private JsonNode readResourceFileContent(String path) {
        try(InputStream is = DefaultJsonSchemaValidatorTest.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }

            return objectMapper.readTree(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
