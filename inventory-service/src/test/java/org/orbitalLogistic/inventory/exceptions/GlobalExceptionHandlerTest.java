package org.orbitalLogistic.inventory.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleInventoryTransactionNotFound() {
        InventoryTransactionNotFoundException ex = new InventoryTransactionNotFoundException("tx missing");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleInventoryTransactionNotFoundException(ex);
        assertEquals(404, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals("tx missing", resp.getBody().message());
        assertEquals("Not Found", resp.getBody().error());
    }

    @Test
    void handleCargoManifestNotFound() {
        CargoManifestNotFoundException ex = new CargoManifestNotFoundException("manifest missing");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleCargoManifestNotFoundException(ex);
        assertEquals(404, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals("manifest missing", resp.getBody().message());
    }

    @Test
    void handleInvalidOperation() {
        InvalidOperationException ex = new InvalidOperationException("bad op");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleInvalidOperationException(ex);
        assertEquals(400, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals("bad op", resp.getBody().message());
        assertEquals("Bad Request", resp.getBody().error());
    }

    @Test
    void handleUserServiceException() {
        UserServiceException ex = new UserServiceException("user svc down");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleUserServiceException(ex);
        assertEquals(503, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals("user svc down", resp.getBody().message());
    }

    @Test
    void handleSpacecraftServiceException() {
        SpacecraftServiceException ex = new SpacecraftServiceException("sc svc down");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleSpacecraftServiceException(ex);
        assertEquals(503, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals("sc svc down", resp.getBody().message());
    }

    @Test
    void handleCargoServiceException() {
        CargoServiceException ex = new CargoServiceException("cargo svc down");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleCargoServiceException(ex);
        assertEquals(503, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals("cargo svc down", resp.getBody().message());
    }

    @Test
    void handleUserServiceNotFound() {
        UserServiceNotFound ex = new UserServiceNotFound("user not found");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleUserServiceNotFound(ex);
        assertEquals(404, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals("user not found", resp.getBody().message());
        assertEquals("User not found", resp.getBody().error());
    }

    @Test
    void handleAllUncaughtException() {
        Exception ex = new Exception("boom");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleAllUncaughtException(ex);
        assertEquals(500, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals("An unexpected error occurred", resp.getBody().message());
    }
}

