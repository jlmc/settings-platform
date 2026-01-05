package io.github.jlmc.settings.client.core.exceptions;

public abstract class AbstractSettingsClientException extends RuntimeException {

    protected AbstractSettingsClientException(String message) {
        super(message);
    }

    protected AbstractSettingsClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
