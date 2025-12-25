package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities;

import tools.jackson.databind.node.ObjectNode;

public record JsonSchema(
        String schemaType,
        ObjectNode schemaContent
) {

    public JsonSchema {
        if (schemaType == null || schemaType.isBlank()) {
            throw new IllegalArgumentException("schemaType must not be null or blank");
        }
        if (schemaContent == null || schemaContent.isEmpty()) {
            throw new IllegalArgumentException("schemaContent must not be null or blank");
        }
    }
}
