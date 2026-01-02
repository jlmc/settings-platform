package io.gihub.jlmc.poc.commons.settings.redis.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.gihub.jlmc.poc.commons.settings.ConfigurationType;

import java.util.List;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResolvedConfiguration(
        String accountId,
        String serviceName,
        ConfigurationType configurationType,
        ServiceJsonSchemas serviceJsonSchemas,
        List<SettingsAccount> settingsAccounts,
        Map<String, Object> mergedSettings
) {
}
