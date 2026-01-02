package io.github.jlmc.settings.service.domain.services;

import io.github.jlmc.settings.service.domain.entities.ResolvedConfiguration;
import io.github.jlmc.settings.service.domain.inputs.ResolveConfigurationInput;

import java.util.Map;

public interface ConfigurationDecryptionService {

    Map<String, Object> decryptForReturn(
            ResolveConfigurationInput input,
            ResolvedConfiguration resolved
    );
}
