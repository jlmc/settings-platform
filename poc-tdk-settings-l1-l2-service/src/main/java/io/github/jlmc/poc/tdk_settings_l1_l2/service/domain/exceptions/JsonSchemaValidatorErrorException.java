package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.exceptions;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonValidationResult;

import java.util.List;
import java.util.Map;

public class JsonSchemaValidatorErrorException extends RuntimeException {

    private final Map<String, List<JsonValidationResult>> invalidSchemas;

    public JsonSchemaValidatorErrorException(String message, Map<String, List<JsonValidationResult>> invalidSchemas) {
        super(message);
        this.invalidSchemas = invalidSchemas;
    }

    public Map<String, List<JsonValidationResult>> getInvalidSchemas() {
        return Map.copyOf(invalidSchemas);
    }
}
