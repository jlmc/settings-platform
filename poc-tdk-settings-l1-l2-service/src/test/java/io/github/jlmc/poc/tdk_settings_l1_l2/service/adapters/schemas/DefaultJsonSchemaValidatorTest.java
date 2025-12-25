package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.schemas;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonValidationError;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultJsonSchemaValidatorTest {

    ObjectMapper objectMapper = new ObjectMapper();

    DefaultJsonSchemaValidator victim;

    @BeforeEach
    void setUp() {
        victim = new DefaultJsonSchemaValidator();
    }

    @Test
    void validateJsonSchemaWithSuccessV7() {
        String payload = readResourceFileContent("/schemas/valid-schema.json");
        JsonNode jsonNode = objectMapper.readTree(payload);

        JsonValidationResult result = victim.validate(jsonNode);

        assertTrue(result.valid());
    }

    @Test
    void validateJsonSchemaWithErrosV7() {
        String payload = readResourceFileContent("/schemas/invalid-schema.json");
        JsonNode jsonNode = objectMapper.readTree(payload);

        JsonValidationResult result = victim.validate(jsonNode);

        assertFalse(result.valid());
        assertEquals(2L, result.errors().size());
        assertEquals(
                Set.of(
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
                ), new HashSet<>(result.errors())
        );
    }

    private String readResourceFileContent(String s) {
        try(InputStream resourceAsStream = DefaultJsonSchemaValidatorTest.class.getResourceAsStream(s)) {
            byte[] bytes = resourceAsStream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
