package io.github.jlmc.settings.service.domain.services;

import io.github.jlmc.settings.service.domain.entities.ResolvedConfiguration;
import io.github.jlmc.settings.service.domain.inputs.ResolveConfigurationInput;

public interface ConfigurationResolver {

    ResolvedConfiguration resolve(ResolveConfigurationInput input);
}
