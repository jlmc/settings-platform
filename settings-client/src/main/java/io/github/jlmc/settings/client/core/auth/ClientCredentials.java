package io.github.jlmc.settings.client.core.auth;

import java.util.List;
import java.util.Objects;

/// Represents OAuth2 client credentials for performing token-based authentication.
///
/// The '/oauth/token' path will be appended automatically when requesting tokens.
public record ClientCredentials(
        String clientId,
        String clientSecret,
        String tokenUrl,
        List<String> scopes
) implements AuthCredentials {

    public ClientCredentials {
        Objects.requireNonNull(clientId, "Client ID cannot be null");
        Objects.requireNonNull(clientSecret, "Client secret cannot be null");
        Objects.requireNonNull(tokenUrl, "Token URL cannot be null");
        Objects.requireNonNull(scopes, "Scopes cannot be null");

        if (clientId.isBlank()) {
            throw new IllegalArgumentException("Client ID cannot be blank.");
        }
        if (clientSecret.isBlank()) {
            throw new IllegalArgumentException("Client secret cannot be blank.");
        }
        if (tokenUrl.isBlank()) {
            throw new IllegalArgumentException("Token URL cannot be blank.");
        }
        if (!tokenUrl.startsWith("http")) {
            throw new IllegalArgumentException("Token URL must start with http or https: '" + tokenUrl + "'");
        }
    }

    /**
     * Convenience constructor with default scope.
     */
    public ClientCredentials(String clientId, String clientSecret, String tokenUrl) {
        this(clientId, clientSecret, tokenUrl, List.of("industries-settings:read"));
    }
}
