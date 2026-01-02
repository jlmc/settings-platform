package io.github.jlmc.settings.client.token;

import io.github.jlmc.settings.client.auth.AuthCredentials;
import io.github.jlmc.settings.client.exceptions.SettingsClientException;

public interface AccessTokenProvider {

    String acquireToken(AuthCredentials credentials) throws SettingsClientException;
}
