package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.adapters.settings;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "tdk.configurations-settings")
public record SettingsConfigurationProperties(
        @NotBlank String apiBaseUrl,
        @DefaultValue("settings") String namespace,
        @DefaultValue("true") boolean redisEnabled,
        @DefaultValue("PT10H") Duration redisL1Ttl,
        List<String> services
) {
}
