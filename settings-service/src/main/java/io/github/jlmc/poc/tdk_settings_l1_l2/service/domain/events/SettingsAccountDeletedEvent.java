package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;

public record SettingsAccountDeletedEvent(SettingsAccount settingsAccount) {
}
