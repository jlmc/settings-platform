package io.github.jlmc.settings.service.domain.events;

import io.github.jlmc.settings.service.domain.entities.ResolvedConfiguration;

public record ConfigurationHitEvent(ResolvedConfiguration resolvedConfiguration) {
}
