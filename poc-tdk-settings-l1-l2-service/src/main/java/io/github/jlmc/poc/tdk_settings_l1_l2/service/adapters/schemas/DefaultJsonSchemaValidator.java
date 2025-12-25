package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.schemas;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.DialectId;
import com.networknt.schema.dialect.Dialects;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonSchema;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonValidationError;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonValidationResult;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.JsonSchemaValidator;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class DefaultJsonSchemaValidator implements JsonSchemaValidator {

    public static final String SCHEMAS_SCHEMAV_7_JSON = "/schemas/schemav7.json";
    private final Schema schema;

    public DefaultJsonSchemaValidator() {
        this.schema = loadSchema();
    }

    private Schema loadSchema() {
        try (InputStream schemaStream = getClass().getResourceAsStream(SCHEMAS_SCHEMAV_7_JSON)) {
            if (schemaStream == null) {
                throw new IllegalStateException("Schema file not found: " + SCHEMAS_SCHEMAV_7_JSON);
            }

            SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft7());

            return schemaRegistry.getSchema(schemaStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public JsonValidationResult validate(JsonNode json) {
        List<Error> errors = schema.validate(json);

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

    @Override
    public JsonValidationResult validate(JsonSchema json) {
        return validate(json.schemaContent());
    }
}
