package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities;

import java.util.List;
import java.util.Objects;

public record ServiceJsonSchemas(
        String serviceName,
        List<JsonSchema> schemas,
        Rsa rsa
) {
    public ServiceJsonSchemas {
        if (serviceName == null) {
            throw new IllegalArgumentException("serviceName cannot be null");
        }

        Objects.requireNonNull(serviceName, "serviceName cannot be null");

        if (serviceName.isBlank()) {
            throw new IllegalArgumentException("serviceName cannot be blank");
        }

        if (schemas == null || schemas.isEmpty()) {
            throw new IllegalArgumentException("schemas list cannot be empty");
        }
    }

}
