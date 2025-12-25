package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonSchema;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonValidationResult;

import java.util.List;

public interface JsonSchemaValidator {

    JsonValidationResult validate(JsonSchema json);


    JsonValidationResult VALID = new JsonValidationResult(true, List.of());


}
