package io.github.jlmc.settings.se.example.application.out;

import io.github.jlmc.settings.domain.entities.ConfigurationType;
import io.github.jlmc.settings.se.example.domain.MyConfig;

public interface ConfigurationPort {
    MyConfig getConfiguration(String accountId, String serviceName, ConfigurationType type, String typeId);
}
