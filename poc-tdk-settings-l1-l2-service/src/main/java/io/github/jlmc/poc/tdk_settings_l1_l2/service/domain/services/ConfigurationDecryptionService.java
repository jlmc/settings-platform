package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.services;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ResolvedConfiguration;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.inputs.ResolveConfigurationInput;

import java.util.Map;

public interface ConfigurationDecryptionService {

    Map<String, Object> decryptForReturn(
            ResolveConfigurationInput input,
            ResolvedConfiguration resolved
    );
}
