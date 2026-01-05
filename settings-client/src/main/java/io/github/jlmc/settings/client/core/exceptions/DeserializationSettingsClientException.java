package io.github.jlmc.settings.client.core.exceptions;

public class DeserializationSettingsClientException extends AbstractSettingsClientException {

    public DeserializationSettingsClientException(String message) {
        super(message);
    }

    public DeserializationSettingsClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
