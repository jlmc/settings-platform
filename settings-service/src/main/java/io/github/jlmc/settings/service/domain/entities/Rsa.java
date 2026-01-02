package io.github.jlmc.settings.service.domain.entities;

public record Rsa(
    String publicKey
) {
    public Rsa {
        if (publicKey == null || publicKey.isBlank()) {
            throw new IllegalArgumentException("publicKey must not be null or blank");
        }
    }
}
