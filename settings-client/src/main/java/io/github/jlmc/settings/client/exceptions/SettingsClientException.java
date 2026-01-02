package io.github.jlmc.settings.client.exceptions;

public class SettingsClientException extends AbstractSettingsClientException {

    public SettingsClientException(String message) {
        super(message);
    }

    public SettingsClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
