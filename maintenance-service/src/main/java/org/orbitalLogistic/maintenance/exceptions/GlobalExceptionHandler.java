package org.orbitalLogistic.maintenance.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaintenanceLogNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleMaintenanceLogNotFoundException(
            MaintenanceLogNotFoundException ex) {

        log.warn("Maintenance log not found: {}", ex.getMessage());

        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(body));
    }

    @ExceptionHandler(InvalidOperationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidOperationException(
            InvalidOperationException ex) {

        log.warn("Invalid operation: {}", ex.getMessage());

        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ValidationErrorResponse>> handleWebExchangeBindException(
            WebExchangeBindException ex) {

        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });

        ValidationErrorResponse body = new ValidationErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Validation failed",
                errors
        );

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<ValidationErrorResponse>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });

        ValidationErrorResponse body = new ValidationErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Validation failed",
                errors
        );

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAllExceptions(Exception ex) {

        log.error("Internal error: {}", ex.getMessage(), ex);

        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Unexpected error occurred"
        );

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
    }

    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message
    ) {}

    public record ValidationErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            Map<String, String> details
    ) {}
}
