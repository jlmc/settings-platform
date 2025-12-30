package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;

import java.util.List;
import java.util.Map;

public record ConfigurationHitEvent(
        String accountId,
        String serviceName,
        ConfigurationType configurationType,
        List<SettingsAccount> settingsAccounts,
        Map<String, Object> mergedSettings
) {
}
