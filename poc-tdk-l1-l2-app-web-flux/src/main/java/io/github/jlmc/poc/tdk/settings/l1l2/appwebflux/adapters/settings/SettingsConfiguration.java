package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.adapters.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gihub.jlmc.poc.commons.settings.IndustriesSettingsClient;
import io.gihub.jlmc.poc.commons.settings.json.JacksonJsonDeserializer;
import io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.ports.IndustriesSettingsProviderPort;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SettingsConfigurationProperties.class)
public class SettingsConfiguration {

    @Bean
    public IndustriesSettingsClient industriesSettingsClient(
            SettingsConfigurationProperties properties,
            ObjectMapper objectMapper
    ) {
        IndustriesSettingsClient industriesSettingsClient =
                IndustriesSettingsClient.builder()
                        .apiBaseUrl(properties.apiBaseUrl())
                        .jsonDeserializer(new JacksonJsonDeserializer(objectMapper))
                        .useRetryExecutor(false)
                        .userAgent("tdk-l1-l2-app-webflux")
                        .build();


        return industriesSettingsClient;
    }

    @Bean
    public IndustriesSettingsProviderPort industriesSettingsProviderPort(IndustriesSettingsClient industriesSettingsClient) {
        return new DefaultIndustriesSettingsProviderPort(industriesSettingsClient);
    }


}
