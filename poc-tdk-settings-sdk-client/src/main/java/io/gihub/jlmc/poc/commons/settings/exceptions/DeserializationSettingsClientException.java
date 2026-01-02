package io.gihub.jlmc.poc.commons.settings.exceptions;

public class DeserializationSettingsClientException extends AbstractSettingsClientException {

    public DeserializationSettingsClientException(String message) {
        super(message);
    }

    public DeserializationSettingsClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
