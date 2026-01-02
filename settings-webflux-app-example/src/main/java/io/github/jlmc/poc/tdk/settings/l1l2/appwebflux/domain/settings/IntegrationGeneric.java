package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class) // camelCase conversion
@JsonIgnoreProperties(ignoreUnknown = true) // ignore unknown fields
public record IntegrationGeneric(
    String subscriptionKey,
    String environment
) {
}
