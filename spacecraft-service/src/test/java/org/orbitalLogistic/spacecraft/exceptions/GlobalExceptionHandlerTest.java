package org.orbitalLogistic.spacecraft.exceptions;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.codec.DecodingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    @DisplayName("Обработка SpacecraftNotFoundException - возвращает 404")
    void handleSpacecraftNotFoundException_Returns404() {
        SpacecraftNotFoundException exception = new SpacecraftNotFoundException("Spacecraft with id 123 not found");

        Mono<ResponseEntity<GlobalExceptionHandler.ErrorResponse>> result =
                exceptionHandler.handleSpacecraftNotFoundException(exception);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(404, response.getBody().status());
                    assertEquals("Not Found", response.getBody().error());
                    assertEquals("Spacecraft with id 123 not found", response.getBody().message());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Обработка SpacecraftAlreadyExistsException - возвращает 409")
    void handleSpacecraftAlreadyExistsException_Returns409() {
        SpacecraftAlreadyExistsException exception =
                new SpacecraftAlreadyExistsException("Spacecraft with registry code SC-001 already exists");

        Mono<ResponseEntity<GlobalExceptionHandler.ErrorResponse>> result =
                exceptionHandler.handleSpacecraftAlreadyExistsException(exception);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(409, response.getBody().status());
                    assertEquals("Conflict", response.getBody().error());
                    assertTrue(response.getBody().message().contains("SC-001"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Обработка SpacecraftTypeNotFoundException - возвращает 404")
    void handleSpacecraftTypeNotFoundException_Returns404() {
        SpacecraftTypeNotFoundException exception =
                new SpacecraftTypeNotFoundException("Spacecraft type with id 1 not found");

        Mono<ResponseEntity<GlobalExceptionHandler.ErrorResponse>> result =
                exceptionHandler.handleSpacecraftTypeNotFoundException(exception);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(404, response.getBody().status());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Обработка SpacecraftCargoUsageException - возвращает 503")
    void handleSpacecraftCargoUsageException_Returns503() {
        SpacecraftCargoUsageException exception =
                new SpacecraftCargoUsageException("Cargo service is unavailable");

        Mono<ResponseEntity<GlobalExceptionHandler.ErrorResponse>> result =
                exceptionHandler.handleSpacecraftCargoUsageException(exception);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(503, response.getBody().status());
                    assertEquals("Cargo service unavailable!", response.getBody().error());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Обработка DataNotFoundException - возвращает 404")
    void handleDataNotFoundException_Returns404() {
        DataNotFoundException exception = new DataNotFoundException("Data not found");

        Mono<ResponseEntity<GlobalExceptionHandler.ErrorResponse>> result =
                exceptionHandler.handleDataNotFoundException(exception);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(404, response.getBody().status());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Обработка IllegalArgumentException - возвращает 400")
    void handleIllegalArgumentException_Returns400() {
        IllegalArgumentException exception =
                new IllegalArgumentException("Invalid registry code format");

        Mono<ResponseEntity<GlobalExceptionHandler.ErrorResponse>> result =
                exceptionHandler.handleIllegalArgument(exception);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(400, response.getBody().status());
                    assertEquals("Invalid registry code format", response.getBody().message());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Обработка ServerWebInputException - возвращает 400 с сообщением об ошибке")
    void handleServerWebInputException_Returns400() {
        ServerWebInputException exception = new ServerWebInputException("Invalid input");

        Mono<ResponseEntity<GlobalExceptionHandler.ErrorResponse>> result =
                exceptionHandler.handleServerWebInputException(exception);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(400, response.getBody().status());
                    assertNotNull(response.getBody().message());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Обработка DecodingException с ValueInstantiationException - извлекает правильное сообщение")
    void handleDecodingException_WithValueInstantiationException_ExtractsMessage() {
        IllegalArgumentException rootCause =
                new IllegalArgumentException("Unknown SpacecraftClassification value: 'INVALID'");
        ValueInstantiationException valueCause = mock(ValueInstantiationException.class);
        when(valueCause.getCause()).thenReturn(rootCause);
        DecodingException exception = new DecodingException("Decoding error", valueCause);

        Mono<ResponseEntity<GlobalExceptionHandler.ErrorResponse>> result =
                exceptionHandler.handleDecodingException(exception);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertTrue(response.getBody().message().contains("Unknown SpacecraftClassification"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Обработка AuthorizationDeniedException - возвращает 403")
    void handleAuthorizationDeniedException_Returns403() {
        org.springframework.security.authorization.AuthorizationDecision authorizationDecision =
                new org.springframework.security.authorization.AuthorizationDecision(false);

        AuthorizationDeniedException exception =
                new AuthorizationDeniedException("Access denied", authorizationDecision);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> result =
                exceptionHandler.handleAuthorizationDeniedException(exception);

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(403, result.getBody().status());
        assertEquals("Forbidden", result.getBody().error());
    }

    @Test
    @DisplayName("Обработка DataIntegrityViolationException - возвращает 409")
    void handleDataIntegrityViolation_Returns409() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("Data integrity violation");

        Mono<ResponseEntity<GlobalExceptionHandler.ErrorResponse>> result =
                exceptionHandler.handleDataIntegrityViolation(exception);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(409, response.getBody().status());
                    assertEquals("Conflict", response.getBody().error());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Обработка DataIntegrityViolationException с foreign key - специальное сообщение")
    void handleDataIntegrityViolation_WithForeignKey_ReturnsSpecialMessage() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("foreign key constraint violation");

        Mono<ResponseEntity<GlobalExceptionHandler.ErrorResponse>> result =
                exceptionHandler.handleDataIntegrityViolation(exception);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                    assertTrue(response.getBody().message().contains("referenced by other data"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Обработка ConstraintViolationException - возвращает ValidationErrorResponse")
    void handleConstraintViolationException_ReturnsValidationError() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("registryCode");
        when(violation.getMessage()).thenReturn("must not be blank");
        violations.add(violation);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        Mono<ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse>> result =
                exceptionHandler.handleConstraintViolation(exception);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(400, response.getBody().status());
                    assertEquals("Validation failed", response.getBody().message());
                    assertFalse(response.getBody().details().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Обработка общего Exception - возвращает 500")
    void handleGeneralException_Returns500() {
        Exception exception = new RuntimeException("Unexpected error");

        Mono<ResponseEntity<GlobalExceptionHandler.ErrorResponse>> result =
                exceptionHandler.handleAllUnhandledExceptions(exception);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(500, response.getBody().status());
                    assertEquals("Internal Server Error", response.getBody().error());
                    assertEquals("An unexpected error occurred", response.getBody().message());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("ErrorResponse record - создается корректно")
    void errorResponse_CreatesCorrectly() {
        var timestamp = java.time.LocalDateTime.now();
        var response = new GlobalExceptionHandler.ErrorResponse(
                timestamp, 404, "Not Found", "Resource not found"
        );

        assertEquals(timestamp, response.timestamp());
        assertEquals(404, response.status());
        assertEquals("Not Found", response.error());
        assertEquals("Resource not found", response.message());
    }

    @Test
    @DisplayName("ValidationErrorResponse record - создается корректно")
    void validationErrorResponse_CreatesCorrectly() {
        var timestamp = java.time.LocalDateTime.now();
        var details = java.util.Map.of("field1", "error1", "field2", "error2");
        var response = new GlobalExceptionHandler.ValidationErrorResponse(
                timestamp, 400, "Bad Request", "Validation failed", details
        );

        assertEquals(timestamp, response.timestamp());
        assertEquals(400, response.status());
        assertEquals("Bad Request", response.error());
        assertEquals("Validation failed", response.message());
        assertEquals(2, response.details().size());
        assertTrue(response.details().containsKey("field1"));
        assertTrue(response.details().containsKey("field2"));
    }
}

