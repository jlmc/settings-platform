package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import tools.jackson.databind.node.ObjectNode;

@Valid
public record JsonSchemaRepresentation(
        @NotBlank
        @Pattern(
                regexp = ConfigurationType.PATTERN,
                flags = {Pattern.Flag.CASE_INSENSITIVE}
        )
        String type,
        @NotNull
        ObjectNode schemaContent
) {
}
