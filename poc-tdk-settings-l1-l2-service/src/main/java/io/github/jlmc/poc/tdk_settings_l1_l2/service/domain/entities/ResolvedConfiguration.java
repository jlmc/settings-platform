package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities;

import java.util.List;
import java.util.Map;

public record ResolvedConfiguration(
        String accountId,
        String serviceName,
        ConfigurationType configurationType,
        ServiceJsonSchemas serviceJsonSchemas,
        List<SettingsAccount> settingsAccounts,
        Map<String, Object> mergedSettings
) {
}
