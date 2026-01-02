package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.Rsa;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Valid
public record ServiceJsonSchemasRepresentation(
    @NotBlank  String serviceName,
    @NotNull @NotEmpty List<@Valid JsonSchemaRepresentation> jsonSchemas,
    @Valid Rsa rsa
) {
}
