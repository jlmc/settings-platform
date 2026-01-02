package io.github.jlmc.settings.service.adapters.http.data;

import tools.jackson.databind.node.ObjectNode;

public record SettingsAccountRepresentation(
        String type,
        String accountId,
        String serviceName,
        ObjectNode content
) {
}
