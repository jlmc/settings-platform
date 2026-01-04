package io.github.jlmc.settings.se.example.application.service;

import io.github.jlmc.settings.domain.entities.ConfigurationType;
import io.github.jlmc.settings.se.example.application.in.GetConfigurationUseCase;
import io.github.jlmc.settings.se.example.application.out.ConfigurationPort;
import io.github.jlmc.settings.se.example.domain.MyConfig;

public class GetConfigurationService implements GetConfigurationUseCase {

    private final ConfigurationPort configurationPort;

    public GetConfigurationService(ConfigurationPort configurationPort) {
        this.configurationPort = configurationPort;
    }

    @Override
    public MyConfig execute(String accountId, String serviceName, ConfigurationType type, String typeId) {
        return configurationPort.getConfiguration(accountId, serviceName, type, typeId);
    }
}
