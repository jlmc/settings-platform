package io.github.jlmc.settings.service.domain.entities;

import java.util.List;

public record JsonValidationResult(
        boolean valid,
        List<JsonValidationError> errors
) {
}
