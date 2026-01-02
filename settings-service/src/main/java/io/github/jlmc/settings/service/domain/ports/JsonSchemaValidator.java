package io.github.jlmc.settings.service.domain.ports;

import io.github.jlmc.settings.service.domain.entities.JsonSchema;
import io.github.jlmc.settings.service.domain.entities.JsonValidationResult;

import java.util.List;

public interface JsonSchemaValidator {

    JsonValidationResult validate(JsonSchema json);

    JsonValidationResult VALID = new JsonValidationResult(true, List.of());

}
