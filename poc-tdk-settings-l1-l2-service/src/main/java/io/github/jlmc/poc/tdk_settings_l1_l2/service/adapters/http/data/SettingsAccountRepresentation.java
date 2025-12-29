package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data;

import tools.jackson.databind.node.ObjectNode;

public record SettingsAccountRepresentation(
        String type,
        String accountId,
        String serviceName,
        ObjectNode content
) {
}
