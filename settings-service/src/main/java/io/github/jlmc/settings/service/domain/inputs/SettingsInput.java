package io.github.jlmc.settings.service.domain.inputs;

import io.github.jlmc.settings.service.domain.entities.ConfigurationType;

public record SettingsInput(
        String accountId,
        String serviceName,
        ConfigurationType type
) { }
