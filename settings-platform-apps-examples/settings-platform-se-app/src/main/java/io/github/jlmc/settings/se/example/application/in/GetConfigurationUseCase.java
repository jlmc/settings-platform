package io.github.jlmc.settings.se.example.application.in;

import io.github.jlmc.settings.domain.entities.ConfigurationType;
import io.github.jlmc.settings.se.example.domain.MyConfig;

public interface GetConfigurationUseCase {
    MyConfig execute(
            String accountId,
            String serviceName,
            ConfigurationType type,
            String typeId
    );
}
