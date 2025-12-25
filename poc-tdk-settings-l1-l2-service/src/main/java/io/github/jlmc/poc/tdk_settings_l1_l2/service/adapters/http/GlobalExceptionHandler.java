package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.exceptions.JsonSchemaValidatorErrorException;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.exceptions.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

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
}
