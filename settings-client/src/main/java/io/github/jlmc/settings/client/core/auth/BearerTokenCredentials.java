package io.github.jlmc.settings.client.core.auth;

import java.util.Objects;

public record BearerTokenCredentials(String token) implements AuthCredentials {

    public BearerTokenCredentials {
        Objects.requireNonNull(token, "Bearer token cannot be null");
        if (token.isBlank()) {
            throw new IllegalArgumentException("Bearer token cannot be blank.");
        }
    }

    @Override
    public String toString() {
        return "BearerTokenCredentials(token=***REDACTED***)";
    }
}
