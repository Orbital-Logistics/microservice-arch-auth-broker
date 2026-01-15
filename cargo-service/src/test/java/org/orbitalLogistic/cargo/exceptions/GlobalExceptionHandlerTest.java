package org.orbitalLogistic.cargo.exceptions;

import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.domain.exception.*;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.exception.GlobalExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final WebRequest webRequest = new ServletWebRequest(new MockHttpServletRequest());

    @Test
    void handleCargoNotFound() {
        CargoNotFoundException ex = new CargoNotFoundException("Cargo not found X");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleCargoNotFoundException(ex, webRequest);
        assertEquals(HttpStatus.NOT_FOUND.value(), resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertEquals("Cargo not found X", resp.getBody().message());
        assertEquals("Not Found", resp.getBody().error());
    }

    @Test
    void handleCargoAlreadyExists() {
        CargoAlreadyExistsException ex = new CargoAlreadyExistsException("Already exists");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleCargoAlreadyExistsException(ex, webRequest);
        assertEquals(HttpStatus.CONFLICT.value(), resp.getStatusCode().value());
        assertEquals("Already exists", resp.getBody().message());
        assertEquals("Conflict", resp.getBody().error());
    }

    @Test
    void handleCargoCategoryNotFound() {
        CargoCategoryNotFoundException ex = new CargoCategoryNotFoundException("Category missing");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleCargoCategoryNotFoundException(ex, webRequest);
        assertEquals(HttpStatus.NOT_FOUND.value(), resp.getStatusCode().value());
        assertEquals("Category missing", resp.getBody().message());
        assertEquals("Not Found", resp.getBody().error());
    }

    @Test
    void handleStorageUnitNotFound() {
        StorageUnitNotFoundException ex = new StorageUnitNotFoundException("SU missing");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleStorageUnitNotFoundException(ex, webRequest);
        assertEquals(HttpStatus.NOT_FOUND.value(), resp.getStatusCode().value());
        assertEquals("SU missing", resp.getBody().message());
        assertEquals("Not Found", resp.getBody().error());
    }

    @Test
    void handleUserNotFound() {
        UserNotFoundException ex = new UserNotFoundException("User not found 1");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleUserNotFoundException(ex, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), resp.getStatusCode().value());
        assertEquals("User not found 1", resp.getBody().message());
        assertEquals("Bad Request", resp.getBody().error());
    }

    @Test
    void handleUserServiceException() {
        UserServiceException ex = new UserServiceException("Service down");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleUserServiceException(ex, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), resp.getStatusCode().value());
        assertEquals("Service down", resp.getBody().message());
        assertEquals("Bad Request", resp.getBody().error());
    }

    @Test
    void handleStorageUnitAlreadyExists() {
        StorageUnitAlreadyExistsException ex = new StorageUnitAlreadyExistsException("SU exists");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleStorageUnitAlreadyExistsException(ex, webRequest);
        assertEquals(HttpStatus.CONFLICT.value(), resp.getStatusCode().value());
        assertEquals("SU exists", resp.getBody().message());
        assertEquals("Conflict", resp.getBody().error());
    }

    @Test
    void handleCargoStorageNotFound() {
        CargoStorageNotFoundException ex = new CargoStorageNotFoundException("CS missing");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleCargoStorageNotFoundException(ex, webRequest);
        assertEquals(HttpStatus.NOT_FOUND.value(), resp.getStatusCode().value());
        assertEquals("CS missing", resp.getBody().message());
        assertEquals("Not Found", resp.getBody().error());
    }

    @Test
    void handleInsufficientCapacity() {
        InsufficientCapacityException ex = new InsufficientCapacityException("no capacity");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleInsufficientCapacityException(ex, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), resp.getStatusCode().value());
        assertEquals("no capacity", resp.getBody().message());
        assertEquals("Bad Request", resp.getBody().error());
    }

    @Test
    void handleCargoInUse() {
        CargoInUseException ex = new CargoInUseException("Cargo is in use");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleCargoInUseException(ex, webRequest);
        assertEquals(HttpStatus.CONFLICT.value(), resp.getStatusCode().value());
        assertEquals("Cargo is in use", resp.getBody().message());
        assertEquals("Conflict", resp.getBody().error());
    }
}

