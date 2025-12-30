package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.configurations;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ResolvedConfiguration;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.services.ConfigurationDecryptionService;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.services.ConfigurationResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class GetConfigurationUseCase {

    private final ConfigurationResolver configurationResolver;
    private final ConfigurationDecryptionService configurationDecryptionService;

    public GetConfigurationUseCase(ConfigurationResolver configurationResolver, ConfigurationDecryptionService configurationDecryptionService) {
        this.configurationResolver = configurationResolver;
        this.configurationDecryptionService = configurationDecryptionService;
    }

    public Map<String, Object> execute(Input input) {

        ResolvedConfiguration resolvedConfiguration = configurationResolver.resolve(input);

        if (!input.hasPrivateKey()) {
            return resolvedConfiguration.mergedSettings();
        }


        return configurationDecryptionService.decryptForReturn(input, resolvedConfiguration);
    }

}
