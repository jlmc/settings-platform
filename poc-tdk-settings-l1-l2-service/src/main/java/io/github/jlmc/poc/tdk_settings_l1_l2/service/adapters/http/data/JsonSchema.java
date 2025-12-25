package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Valid
public record JsonSchema(
    @NotBlank String type,
    Object value
) {
}


/*
data class SchemaDto(
    @field:NotBlank @field:Pattern(regexp = "SERVICE|ACCOUNT|RING_GROUP|AGENT|INTERACTION|CLIENT") val type: String?,
    @field:JsonRequired var value: JsonContent,
)
 */