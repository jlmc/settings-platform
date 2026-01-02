package io.github.jlmc.settings.service.domain.exceptions;

import io.github.jlmc.settings.service.domain.entities.JsonValidationError;
import lombok.Getter;

import java.util.List;

@Getter
public class SettingsAccountJsonValidationException extends RuntimeException {
    private final List<JsonValidationError> errors;

    public SettingsAccountJsonValidationException(String message, List<JsonValidationError> errors) {
        super(message);
        this.errors = errors != null ? List.copyOf(errors) : List.of();
    }

}
