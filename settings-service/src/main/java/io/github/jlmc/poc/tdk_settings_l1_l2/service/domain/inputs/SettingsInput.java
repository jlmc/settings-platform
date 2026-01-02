package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.inputs;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;

public record SettingsInput(
        String accountId,
        String serviceName,
        ConfigurationType type
) { }
