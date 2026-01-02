package io.github.jlmc.settings.client.token;

import io.github.jlmc.settings.client.auth.AuthCredentials;
import io.github.jlmc.settings.client.exceptions.SettingsClientException;

/**
 * Defines the contract for acquiring an access token based on specific credential types.
 * This is the Strategy interface in the Strategy Pattern.
 *
 * @param <T> the type of credentials supported by this strategy
 */
public interface TokenAcquisitionStrategy<T extends AuthCredentials> {

    /**
     * The credential type this strategy supports.
     *
     * @return the class of the supported credentials
     */
    Class<T> getSupportedType();

    /**
     * Executes the logic to acquire the token.
     *
     * @param credentials The credentials containing client information.
     * @return The acquired access token string.
     * @throws SettingsClientException if the token acquisition fails (network, auth error, etc.).
     */
    String acquireToken(T credentials) throws SettingsClientException;
}
