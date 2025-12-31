package io.gihub.jlmc.poc.commons.settings.token;

import io.gihub.jlmc.poc.commons.settings.auth.AuthCredentials;
import io.gihub.jlmc.poc.commons.settings.exceptions.SettingsClientException;

public interface AccessTokenProvider {

    String acquireToken(AuthCredentials credentials) throws SettingsClientException;
}
