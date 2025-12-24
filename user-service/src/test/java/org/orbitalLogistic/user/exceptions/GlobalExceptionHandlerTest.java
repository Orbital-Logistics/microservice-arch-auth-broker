package org.orbitalLogistic.user.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.user.exceptions.common.InvalidOperationException;
import org.orbitalLogistic.user.exceptions.auth.UnknownUsernameException;
import org.orbitalLogistic.user.exceptions.auth.UsernameAlreadyExistsException;
import org.orbitalLogistic.user.exceptions.auth.EmailAlreadyExistsException;
import org.orbitalLogistic.user.exceptions.roles.RoleAlreadyExistsException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUnknownUsername() {
        UnknownUsernameException ex = new UnknownUsernameException("not found");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleUnknownUsernameException(ex);
        assertNotNull(resp);
        assertEquals(404, resp.getStatusCodeValue());
        assertEquals("not found", resp.getBody().message());
    }

    @Test
    void handleInvalidOperation() {
        InvalidOperationException ex = new InvalidOperationException("conflict");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleInvalidOperationException(ex);
        assertNotNull(resp);
        assertEquals(409, resp.getStatusCodeValue());
        assertEquals("conflict", resp.getBody().message());
        assertEquals("Conflict", resp.getBody().error());
    }

    @Test
    void handleUsernameAlreadyExists() {
        UsernameAlreadyExistsException ex = new UsernameAlreadyExistsException("exists");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleUsernameAlreadyExistsException(ex);
        assertNotNull(resp);
        assertEquals(409, resp.getStatusCodeValue());
        assertEquals("exists", resp.getBody().message());
    }

    @Test
    void handleDataNotFound() {
        org.orbitalLogistic.user.exceptions.common.DataNotFoundException ex = new org.orbitalLogistic.user.exceptions.common.DataNotFoundException("dnot");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleDataNotFoundException(ex);
        assertNotNull(resp);
        assertEquals(404, resp.getStatusCodeValue());
        assertEquals("dnot", resp.getBody().message());
    }

    @Test
    void handleMethodArgumentNotValid() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult br = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(br);
        FieldError fe = new FieldError("obj", "field1", "must not be null");
        when(br.getAllErrors()).thenReturn(List.of(fe));

        ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> resp = handler.handleValidationExceptions(ex);
        assertNotNull(resp);
        assertEquals(400, resp.getStatusCodeValue());
        assertEquals("Validation failed", resp.getBody().message());
        assertTrue(resp.getBody().details().containsKey("field1"));
    }

    @Test
    void handleWebExchangeBindException() {
        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        BindingResult br = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(br);
        FieldError fe = new FieldError("obj", "field2", "bad");
        when(br.getAllErrors()).thenReturn(List.of(fe));

        ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> resp = handler.handleWebExchangeBindException(ex);
        assertNotNull(resp);
        assertEquals(400, resp.getStatusCodeValue());
        assertEquals("Validation failed", resp.getBody().message());
        assertEquals("bad", resp.getBody().details().get("field2"));
    }

    @Test
    void handleHttpMessageNotReadable() {
        RuntimeException cause = new RuntimeException("root cause message");
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("msg", cause);
        ResponseEntity<GlobalExceptionHandler.ErrorResponseEnum> resp = handler.handleHttpMessageNotReadable(ex);
        assertNotNull(resp);
        assertEquals(400, resp.getStatusCodeValue());
        assertEquals("MALFORMED_JSON", resp.getBody().code());
        assertEquals("root cause message", resp.getBody().message());
        assertTrue(resp.getBody().details().containsKey("cause"));
    }

    @Test
    void handleConstraintViolation() {
        ConstraintViolation<?> cv = mock(ConstraintViolation.class);
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);
        when(path.toString()).thenReturn("x.y");
        when(cv.getPropertyPath()).thenReturn(path);
        when(cv.getMessage()).thenReturn("must be valid");
        Set<ConstraintViolation<?>> set = Set.of((ConstraintViolation) cv);
        ConstraintViolationException ex = new ConstraintViolationException(set);
        ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> resp = handler.handleConstraintViolation(ex);
        assertNotNull(resp);
        assertEquals(400, resp.getStatusCodeValue());
        assertEquals("Validation failed", resp.getBody().message());
        assertEquals("must be valid", resp.getBody().details().get("x.y"));
    }

    @Test
    void handleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("bad arg");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleIllegalArgument(ex);
        assertNotNull(resp);
        assertEquals(400, resp.getStatusCodeValue());
        assertEquals("bad arg", resp.getBody().message());
    }

    @Test
    void handleServerWebInputException() {
        ServerWebInputException ex = new ServerWebInputException("bad body");
        ResponseEntity<Map<String, String>> resp = handler.handleServerWebInputException(ex);
        assertNotNull(resp);
        assertEquals(400, resp.getStatusCodeValue());
        assertEquals("Invalid request body", resp.getBody().get("error"));
    }

    @Test
    void handleAllUncaughtException() {
        Exception ex = new Exception("boom");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleAllUncaughtException(ex);
        assertNotNull(resp);
        assertEquals(500, resp.getStatusCodeValue());
        assertEquals("An unexpected error occurred", resp.getBody().message());
    }

    @Test
    void handleRoleAlreadyExists() {
        RoleAlreadyExistsException ex = new RoleAlreadyExistsException("role dup");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleRoleAlreadyExistsException(ex);
        assertNotNull(resp);
        assertEquals(409, resp.getStatusCodeValue());
        assertEquals("Conflict, role already exists", resp.getBody().error());
        assertEquals("role dup", resp.getBody().message());
    }
}
