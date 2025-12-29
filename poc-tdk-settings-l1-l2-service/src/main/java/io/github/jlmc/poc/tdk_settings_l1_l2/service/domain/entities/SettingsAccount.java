package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities;

import tools.jackson.databind.node.ObjectNode;

public record SettingsAccount(
        ConfigurationType type,
        String accountId,
        String serviceName,
        ObjectNode schemaContent
) {
}
