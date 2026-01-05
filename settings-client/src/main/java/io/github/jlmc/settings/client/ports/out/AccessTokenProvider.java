package io.github.jlmc.settings.client.ports.out;

import io.github.jlmc.settings.client.core.auth.AuthCredentials;
import io.github.jlmc.settings.client.core.exceptions.SettingsClientException;

public interface AccessTokenProvider {

    String acquireToken(AuthCredentials credentials) throws SettingsClientException;
}
