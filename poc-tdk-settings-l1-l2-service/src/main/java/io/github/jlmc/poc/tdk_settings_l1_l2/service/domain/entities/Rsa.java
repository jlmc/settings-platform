package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities;

public record Rsa(
    String publicKey
) {
    public Rsa {
        if (publicKey == null || publicKey.isBlank()) {
            throw new IllegalArgumentException("publicKey must not be null or blank");
        }
    }
}
