package io.github.jlmc.poc.commons.settings.exceptions;

public abstract class AbstractSettingsClientException extends RuntimeException {

    protected AbstractSettingsClientException(String message) {
        super(message);
    }

    protected AbstractSettingsClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
