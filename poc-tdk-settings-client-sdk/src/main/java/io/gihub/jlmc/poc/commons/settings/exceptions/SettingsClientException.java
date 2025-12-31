package io.gihub.jlmc.poc.commons.settings.exceptions;

public class SettingsClientException extends AbstractSettingsClientException {

    public SettingsClientException(String message) {
        super(message);
    }

    public SettingsClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
