package io.github.jlmc.settings.service.domain.entities;

public record JsonValidationError(
        String message,
        String property,
        Integer index,
        String schemaLocation,
        String keyword
) {
}
