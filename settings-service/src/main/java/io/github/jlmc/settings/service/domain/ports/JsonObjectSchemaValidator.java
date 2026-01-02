package io.github.jlmc.settings.service.domain.ports;

import io.github.jlmc.settings.service.domain.entities.JsonSchema;
import io.github.jlmc.settings.service.domain.entities.JsonValidationResult;
import io.github.jlmc.settings.service.domain.entities.SettingsAccount;

public interface JsonObjectSchemaValidator {

    JsonValidationResult validate(SettingsAccount settingsAccount, JsonSchema jsonSchema);
}
