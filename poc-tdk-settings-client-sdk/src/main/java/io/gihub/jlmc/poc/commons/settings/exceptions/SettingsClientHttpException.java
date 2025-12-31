package io.gihub.jlmc.poc.commons.settings.exceptions;

public class SettingsClientHttpException extends AbstractSettingsClientException {

    private final int statusCode;

    public SettingsClientHttpException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public SettingsClientHttpException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
