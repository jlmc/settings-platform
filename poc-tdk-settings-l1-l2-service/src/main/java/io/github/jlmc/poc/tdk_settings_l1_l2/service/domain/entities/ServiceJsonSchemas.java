package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

        String duplicates = schemas.stream()
                .collect(Collectors.groupingBy(
                        JsonSchema::schemaType,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 1)
                .map(e -> e.getKey().toString())
                .collect(Collectors.joining(", "));

        if (!duplicates.isEmpty()) {
            throw new IllegalArgumentException("Duplicate ConfigurationType(s) found: " + duplicates);
        }
    }

    public Optional<JsonSchema> findByConfigurationType(ConfigurationType type) {
        return schemas.stream().filter(it -> it.schemaType().equals(type)).findFirst();
    }
}
