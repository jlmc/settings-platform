package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ResolvedConfiguration;

public record ConfigurationHitEvent(ResolvedConfiguration resolvedConfiguration) {
}
