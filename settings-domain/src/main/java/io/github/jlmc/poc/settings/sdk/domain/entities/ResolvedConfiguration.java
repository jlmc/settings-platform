package io.github.jlmc.poc.settings.sdk.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

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

    @JsonIgnore
    public boolean hasSchema() {
        return this.serviceJsonSchemas != null;
    }
}
