package io.github.jlmc.settings.client.auth;

import java.util.Objects;

/**
 * Represents privileged client credentials used for OAuth2 JWT-based authentication.
 */
public record PrivilegedCredentials(
        String clientId,
        String clientPrivateKeyB64,
        String clientKeyId,
        String engineerOauthTokenUrl
) implements AuthCredentials {

    public PrivilegedCredentials {
        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(clientKeyId, "clientKeyId must not be null");
        Objects.requireNonNull(clientPrivateKeyB64, "clientPrivateKeyB64 must not be null");
        Objects.requireNonNull(engineerOauthTokenUrl, "engineerOauthTokenUrl must not be null");

        if (clientId.isBlank()) {
            throw new IllegalArgumentException("clientId must not be blank");
        }
        if (clientKeyId.isBlank()) {
            throw new IllegalArgumentException("clientKeyId must not be blank");
        }
        if (clientPrivateKeyB64.isBlank()) {
            throw new IllegalArgumentException("clientPrivateKeyB64 must not be blank");
        }
        if (engineerOauthTokenUrl.isBlank()) {
            throw new IllegalArgumentException("engineerOauthTokenUrl must not be blank");
        }
    }

    @Override
    public String toString() {
        return "PrivilegedCredentials(" +
                "clientId='" + clientId + '\'' +
                ", clientKeyId='" + clientKeyId + '\'' +
                ", engineerOauthTokenUrl='" + engineerOauthTokenUrl + '\'' +
                ", clientPrivateKeyB64='***REDACTED***'" +
                ')';
    }
}
