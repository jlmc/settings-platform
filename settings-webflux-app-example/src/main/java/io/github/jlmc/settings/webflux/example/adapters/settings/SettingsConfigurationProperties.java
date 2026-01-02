package io.github.jlmc.settings.webflux.example.adapters.settings;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "settings.webflux-app.client")

public record SettingsConfigurationProperties(
        @NotBlank String apiBaseUrl,
        @DefaultValue("settings") String namespace,
        @DefaultValue("true") boolean redisEnabled,
        @DefaultValue("PT10H") Duration redisL1Ttl,
        @DefaultValue("1000") long redisL1MaxSize,
        List<String> services
) {
}
