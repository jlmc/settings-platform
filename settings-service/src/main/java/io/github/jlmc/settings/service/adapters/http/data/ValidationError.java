package io.github.jlmc.settings.service.adapters.http.data;

public record ValidationError(
        String field,
        String[] codes,
        String message
) {
    public static ValidationError field(String field, String[] codes, String message) {
        return new ValidationError(field, codes, message);
    }

    public static ValidationError global(String object, String[] codes, String message) {
        return new ValidationError(object, codes, message);
    }
}
