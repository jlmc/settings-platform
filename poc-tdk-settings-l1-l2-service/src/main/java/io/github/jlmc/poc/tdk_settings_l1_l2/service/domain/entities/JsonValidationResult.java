package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities;

import java.util.List;

public record JsonValidationResult(
        boolean valid,
        List<JsonValidationError> errors
) {
}
