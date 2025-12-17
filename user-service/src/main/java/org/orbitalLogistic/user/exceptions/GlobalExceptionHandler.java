package org.orbitalLogistic.user.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.user.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.user.exceptions.user.UserAlreadyExistsException;
import org.orbitalLogistic.user.exceptions.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(InvalidOperationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidOperationException(InvalidOperationException ex) {
        log.warn(ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.warn("User already exists: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage()
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse));
    }


    @ExceptionHandler(DataNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDataNotFoundException(DataNotFoundException ex) {
        log.warn("Data not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage()
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<ValidationErrorResponse>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Validation failed",
            errors
        );

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ValidationErrorResponse>> handleWebExchangeBindException(
            WebExchangeBindException ex) {
        log.warn("WebFlux validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Validation failed",
            errors
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Mono<ResponseEntity<ErrorResponseEnum>> handleInvalidEnumValue(InvalidEnumValueException ex) {
        ErrorResponseEnum errorResponse = new ErrorResponseEnum(
            "INVALID_ENUM_VALUE",
            ex.getMessage(),
            Map.of(
                "field", ex.getFieldName(),
                "invalidValue", ex.getInvalidValue(),
                "acceptedValues", ex.getAcceptedValues()
            )
        );
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAllUncaughtException(Exception ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred"
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

    
    @ExceptionHandler(RoleNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleRoleNotFound(RoleNotFoundException ex) {
        log.warn("Role not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(RoleAlreadyExistsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleRoleAlreadyExists(RoleAlreadyExistsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Already exists",
            ex.getMessage()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse));
    }

    public record ErrorResponseEnum(
        String code,
        String message,
        Map<String, Object> details
    ) {}

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

    @ExceptionHandler(UserAlreadyAssignedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUserAlreadyAssignedException(UserAlreadyAssignedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse));
    }
}
