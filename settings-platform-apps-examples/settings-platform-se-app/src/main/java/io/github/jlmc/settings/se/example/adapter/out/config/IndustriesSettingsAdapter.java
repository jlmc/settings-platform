package io.github.jlmc.settings.se.example.adapter.out.config;

import io.github.jlmc.settings.client.core.ConfigurationRequest;
import io.github.jlmc.settings.client.core.IndustriesSettingsClient;
import io.github.jlmc.settings.client.core.auth.BearerTokenCredentials;
import io.github.jlmc.settings.domain.entities.ConfigurationType;
import io.github.jlmc.settings.se.example.application.out.ConfigurationPort;
import io.github.jlmc.settings.se.example.domain.MyConfig;

public class IndustriesSettingsAdapter implements ConfigurationPort {

    private final IndustriesSettingsClient client;

    public IndustriesSettingsAdapter(IndustriesSettingsClient client) {
        this.client = client;
    }

    @Override
    public MyConfig getConfiguration(String accountId, String serviceName, ConfigurationType type, String typeId) {
        ConfigurationRequest request =
                ConfigurationRequest.standardWithAccountId(
                        new BearerTokenCredentials("header."+accountId+".token"),
                        serviceName,
                        type,
                        typeId,
                        accountId
                );

        return client.getConfiguration(request, MyConfig.class);
    }
}
