package io.github.jlmc.poc.commons.settings.token;

import io.github.jlmc.poc.commons.settings.auth.AuthCredentials;
import io.github.jlmc.poc.commons.settings.exceptions.SettingsClientException;

public interface AccessTokenProvider {

    String acquireToken(AuthCredentials credentials) throws SettingsClientException;
}
