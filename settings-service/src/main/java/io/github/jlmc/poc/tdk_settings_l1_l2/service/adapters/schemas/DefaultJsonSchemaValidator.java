package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.schemas;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonSchema;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonValidationError;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonValidationResult;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.JsonObjectSchemaValidator;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.JsonSchemaValidator;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class DefaultJsonSchemaValidator implements JsonSchemaValidator, JsonObjectSchemaValidator {

    public static final String SCHEMAS_SCHEMA_V_7_JSON = "/schemas/schemav7.json";
    private final Schema schema;

    public DefaultJsonSchemaValidator() {
        this.schema = loadSchema();
    }

    private Schema loadSchema() {
        try (InputStream schemaStream = getClass().getResourceAsStream(SCHEMAS_SCHEMA_V_7_JSON)) {
            if (schemaStream == null) {
                throw new IllegalStateException("Schema file not found: " + SCHEMAS_SCHEMA_V_7_JSON);
            }

            SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft7());

            return schemaRegistry.getSchema(schemaStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public JsonValidationResult validate(JsonNode json) {
        List<Error> errors = schema.validate(json);

        return getJsonValidationResult(errors);
    }

    @Override
    public JsonValidationResult validate(JsonSchema json) {
        return validate(json.schemaContent());
    }


    @Override
    public JsonValidationResult validate(SettingsAccount settingsAccount, JsonSchema jsonSchema) {
        SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft7());
        Schema schema = schemaRegistry.getSchema(jsonSchema.schemaContent());
        List<Error> errors = schema.validate(settingsAccount.content());

        return getJsonValidationResult(errors);
    }

    private static JsonValidationResult getJsonValidationResult(List<Error> errors) {
        if (errors.isEmpty()) {
            return VALID;
        }

        List<JsonValidationError> validationErrors = errors.stream()
                .map(error -> new JsonValidationError(
                        error.getMessage(),
                        error.getInstanceLocation().toString(),
                        error.getIndex(),
                        error.getSchemaLocation().toString(),
                        error.getKeyword()
                ))
                .toList();

        return new JsonValidationResult(false, validationErrors);
    }
}
