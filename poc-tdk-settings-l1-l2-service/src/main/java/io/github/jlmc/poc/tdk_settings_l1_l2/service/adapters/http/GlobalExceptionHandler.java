package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data.ValidationError;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.exceptions.JsonSchemaValidatorErrorException;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.exceptions.NotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private final boolean includeCodesInValidationErrors;

    public GlobalExceptionHandler(Environment environment) {
        final Set<String> codeProfiles = Set.of("dev", "test");

        this.includeCodesInValidationErrors =
                Arrays.stream(environment.getActiveProfiles())
                        .map(String::toLowerCase)
                        .anyMatch(codeProfiles::contains);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Locale locale = request.getLocale();

        BindingResult bindingResult = ex.getBindingResult();
        bindingResult.getAllErrors();

        List<ValidationError> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> toValidationError(error, locale))
                .toList();


        ProblemDetail body = ex.getBody();
        body.setProperty("errors", errors);

        return handleExceptionInternal(ex, body, headers, status, request);
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<Object> handleNotFoundException(NotFoundException ex, WebRequest request) {

        HttpStatus notFound = HttpStatus.NOT_FOUND;
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                notFound,
                ex.getMessage()
        );

        return super.handleExceptionInternal(
                ex,
                problemDetail,
                new HttpHeaders(),
                notFound,
                request
        );
    }

    @ExceptionHandler(JsonSchemaValidatorErrorException.class)
    public ResponseEntity<Object> handleJsonSchemaValidatorErrorException(JsonSchemaValidatorErrorException ex, WebRequest request) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                badRequest,
                ex.getMessage()
        );

        problemDetail.setProperty("errors", ex.getInvalidSchemas());

        return super.handleExceptionInternal(
                ex,
                problemDetail,
                new HttpHeaders(),
                badRequest,
                request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
            java.lang.IllegalArgumentException ex,
            WebRequest request
    ) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                badRequest,
                ex.getMessage()
        );

        return super.handleExceptionInternal(
                ex,
                problemDetail,
                new HttpHeaders(),
                badRequest,
                request
        );
    }

    private String resolveMessage(ObjectError error, Locale locale) {
        MessageSource messageSource = getMessageSource();
        assert error.getCodes() != null;
        for (String code : error.getCodes()) {
            try {
                assert messageSource != null;
                var message = messageSource.getMessage(code, error.getArguments(), locale);
                if (!message.isBlank() && !message.equals(code)) {
                    return message;
                }
            } catch (org.springframework.context.NoSuchMessageException ignored) {
                // Ignore and try next code
            }
        }
        return error.getDefaultMessage();
    }

    private ValidationError toValidationError(ObjectError error, Locale locale) {
        String message = resolveMessage(error, locale);
        String[] codes = includeCodesInValidationErrors ? error.getCodes() : null;

        if (error instanceof FieldError fieldError) {
            return ValidationError.field(
                    fieldError.getField(),
                    codes,
                    message
            );
        }

        return ValidationError.global(
                error.getObjectName(),
                codes,
                message
        );
    }

}
