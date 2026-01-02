package io.gihub.jlmc.poc.commons.settings.redis.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.gihub.jlmc.poc.commons.settings.ConfigurationType;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
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
