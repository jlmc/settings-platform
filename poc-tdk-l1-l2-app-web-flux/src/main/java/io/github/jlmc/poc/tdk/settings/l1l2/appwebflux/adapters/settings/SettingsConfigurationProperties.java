package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.adapters.settings;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "tdk.configurations-settings")
public record SettingsConfigurationProperties(
        @NotBlank String apiBaseUrl
) {
}
