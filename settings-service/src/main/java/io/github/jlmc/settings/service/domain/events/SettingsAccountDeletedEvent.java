package io.github.jlmc.settings.service.domain.events;

import io.github.jlmc.settings.service.domain.entities.SettingsAccount;

public record SettingsAccountDeletedEvent(SettingsAccount settingsAccount) {
}
