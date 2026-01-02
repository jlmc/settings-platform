package io.github.jlmc.settings.service.domain.entities;

import tools.jackson.databind.node.ObjectNode;

public record JsonSchema(
        ConfigurationType schemaType,
        ObjectNode schemaContent
) {

    public JsonSchema {
        if (schemaType == null) {
            throw new IllegalArgumentException("schemaType must not be null");
        }
        if (schemaContent == null || schemaContent.isEmpty()) {
            throw new IllegalArgumentException("content must not be null or blank");
        }
    }
}
