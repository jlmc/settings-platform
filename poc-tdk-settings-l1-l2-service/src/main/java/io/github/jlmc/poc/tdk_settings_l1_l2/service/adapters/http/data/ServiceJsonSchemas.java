package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.Rsa;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Valid
public record ServiceJsonSchemas(
    @NotBlank  String serviceName,
    //String schemaType,
    @NotNull List<@Valid JsonSchema> jsonSchemas,
    Rsa rsa
) {
}


/*
    @field:NotBlank val service: String?,
    @field:NotNull val schemas: List<@Valid SchemaDto>,
    val rsaPublicKey: String? = null
 */