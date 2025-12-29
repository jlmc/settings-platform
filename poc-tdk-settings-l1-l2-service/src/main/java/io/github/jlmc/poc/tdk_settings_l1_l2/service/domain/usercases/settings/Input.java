package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.settings;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;

public record Input(
        String accountId,
        String serviceName,
        ConfigurationType type
) { }
