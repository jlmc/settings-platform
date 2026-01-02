package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities;

public record JsonValidationError(
        String message,
        String property,
        Integer index,
        String schemaLocation,
        String keyword
) {
}
