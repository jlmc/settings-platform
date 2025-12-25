package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities;

import jakarta.validation.constraints.NotBlank;

public record Rsa(
    @NotBlank String publicKey
) {
}
