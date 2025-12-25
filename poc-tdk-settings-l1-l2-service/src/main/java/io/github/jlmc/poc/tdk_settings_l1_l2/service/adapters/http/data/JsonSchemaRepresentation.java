package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.node.ObjectNode;

@Valid
public record JsonSchemaRepresentation(
        @NotBlank String type,
        @NotNull ObjectNode schemaContent
) {
}
