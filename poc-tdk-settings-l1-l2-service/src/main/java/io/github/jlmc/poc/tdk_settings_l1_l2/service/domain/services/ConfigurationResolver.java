package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.services;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ResolvedConfiguration;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.inputs.ResolveConfigurationInput;

public interface ConfigurationResolver {

    ResolvedConfiguration resolve(ResolveConfigurationInput input);
}
